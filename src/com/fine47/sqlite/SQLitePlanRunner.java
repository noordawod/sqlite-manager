/**
 * This file is part of SQLite Manager library. Copyright (C) 2014 Noor Dawod. All
 * rights reserved. https://github.com/noordawod/sqlite-manager
 *
 * Released under the MIT license http://en.wikipedia.org/wiki/MIT_License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.fine47.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.fine47.sqlite.aux.ElasticArrayList;
import com.fine47.sqlite.aux.Util;

/**
 * Orchestrates a {@link SQLiteDatabase}'s upgrade/downgrade process by
 * defining plans and executing them inside a transaction.
 *
 * @see SQLitePlan
 * @since 1.0
 */
public class SQLitePlanRunner implements Runnable {

  private final static String LOG_TAG = "SQLitePlanRunner";

  private final SQLiteDatabase db;
  private final ElasticArrayList<Class<? extends SQLitePlan>> plans;
  private final int targetVersion;

  private Handler handler;
  private int dbVersion;

  private SQLitePlanRunner(SQLiteDatabase db, int targetVersion) {
    if(1 > targetVersion) {
      throw new IllegalArgumentException(
        "Version must be equal or greater than 1.");
    }

    assert null != db;
    assert db.isOpen();
    assert !db.isReadOnly();

    this.db = db;
    this.targetVersion = targetVersion;
    this.plans = new ElasticArrayList();
    this.handler = new DefaultHandler();
  }

  /**
   * Prepares a new {@link SQLitePlanRunner} instance for the specified
   * {@link SQLiteDatabase} and target version.
   *
   * @param db to define plans for
   * @param targetVersion for the plans
   * @return new instance
   */
  public static SQLitePlanRunner For(SQLiteDatabase db, int targetVersion) {
    return new SQLitePlanRunner(db, targetVersion);
  }

  /**
   * Returns the defined {@link Handler} instance for this runner.
   *
   * @return defined handler instance
   */
  public Handler getHandler() {
    return handler;
  }

  /**
   * Defines a {@link Handler} instance for this runner.
   *
   * @param handler to define
   * @return this instance (suitable for chaining)
   */
  public SQLitePlanRunner setHandler(Handler handler) {
    this.handler = handler;
    return this;
  }

  /**
   * Adds a new upgrade/downgrade {@link SQLitePlan} to this database. Versions must
   * start from 1 (inclusive) and advance sequentially.
   *
   * @param version of this {@link SQLitePlan}
   * @param planClass implementing {@link SQLitePlan} interface
   * @return this instance (suitable for chaining)
   */
  public SQLitePlanRunner addPlan(
    int version,
    Class<? extends SQLitePlan> planClass
  ) {
    if(1 > version) {
      throw new IllegalArgumentException(
        "Version must be equal or greater than 1.");
    }
    plans.add(version, planClass);
    return this;
  }

  /**
   * Returns the upgrade/downgrade {@link SQLitePlan} for the specified version.
   *
   * @param version to get {@link SQLitePlan} for
   * @return requested plan if version is valid, NULL otherwise
   */
  public Class<? extends SQLitePlan> getPlan(int version) {
    if(1 > version) {
      throw new IllegalArgumentException(
        "Version must be equal or greater than 1.");
    }
    return plans.get(version);
  }

  /**
   * Executes all plans and performs the upgrade/downgrade operation.
   *
   * @throws SQLiteException when errors occur during upgrade or downgrade
   */
  @Override
  public void run() throws SQLiteException {
    // Get database version.
    dbVersion = db.getVersion();

    // Begin a transaction for all changes.
    db.beginTransaction();

    try {
      // While version of database is different than target version.
      while(dbVersion != targetVersion) {
        // Get the plan at this database version.
        final Class<? extends SQLitePlan> planClass = plans.get(dbVersion);
        if(null == planClass) {
          throw new IllegalStateException(
            "Version " + dbVersion + " has no defined schema plan.");
        }

        // Instantiate this plan.
        final SQLitePlan plan = planClass.newInstance();

        // Database version before schema change.
        final int oldVersion = dbVersion;

        // Determine whether an upgrade or a downgrade is necessary.
        if(dbVersion < targetVersion) {
          // Database version is older than target -- upgrade's necessary.
          plan.applyUpgrade(db);

          // Advance to next version.
          dbVersion++;
        } else {
          // Database version is newer than target -- downgrade necessary.
          plan.applyDowngrade(db);

          // Retract to previous version.
          dbVersion--;
        }

        // Schema change for this version succeeded, update database's version.
        db.setVersion(dbVersion);

        // Notify conductor about the change.
        handler.onChange(db, oldVersion, dbVersion);
      }

      // All plans succeeded.
      db.setTransactionSuccessful();
    } catch(InstantiationException error) {
      Log.e(
        LOG_TAG,
        "Unable to instantiate a new plan for version " + dbVersion,
        error
      );
      handler.onError(db, error);
      throw new SQLiteException(error.getMessage());
    } catch(IllegalAccessException error) {
      Log.e(
        LOG_TAG,
        "Access denied when instantiating a new plan for version " + dbVersion,
        error
      );
      handler.onError(db, error);
      throw new SQLiteException(error.getMessage());
    } catch(SQLiteException error) {
      Log.e(
        LOG_TAG,
        "SQL error when execution plan for version " + dbVersion,
        error
      );
      handler.onError(db, error);
      throw error;
    } finally {
      db.endTransaction();
    }

    // Fire callback when all changes are complete.
    handler.onChangeComplete(db, dbVersion);
  }

  /**
   * A handler interface as the upgrade/downgrade is being performed.
   */
  public static interface Handler {

    /**
     * Notifies implementation that the schema version of the specified database
     * has changed from oldVersion to newVersion.
     *
     * Note that this handler is called for both upgrades and downgrades; it's
     * up to the implementation to determine which is which based on the
     * versions.
     *
     * @param db database being affected
     * @param oldVersion old schema version
     * @param newVersion new schema version
     */
    public void onChange(
      final SQLiteDatabase db,
      int oldVersion,
      int newVersion
    );

    /**
     * Notifies implementation that the schema version of the specified database
     * has now been changed and brought to the specified version.
     *
     * @param db database being affected
     * @param version current database version
     */
    public void onChangeComplete(final SQLiteDatabase db, int version);

    /**
     * Notifies the implementation that an error has occurred in the last
     * operation executed on the specified database.
     *
     * @param db database being affected
     * @param error the caught exception
     */
    public void onError(final SQLiteDatabase db, Throwable error);
  }

  /**
   * Default handler just shows a debug trace of what's happening.
   */
  private static class DefaultHandler implements Handler {

    @Override
    public void onChange(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.i(
        LOG_TAG,
        String.format(
          "onSchemaChange('%s') from V%d to V%d",
          Util.getFileName(db),
          oldVersion,
          newVersion
        )
      );
    }

    @Override
    public void onChangeComplete(SQLiteDatabase db, int version) {
      Log.i(
        LOG_TAG,
        String.format(
          "onSchemaChangeComplete('%s') to V%d",
          Util.getFileName(db),
          version
        )
      );
    }

    @Override
    public void onError(SQLiteDatabase db, Throwable error) {
      Log.e(
        LOG_TAG,
        String.format("onError('%s')", Util.getFileName(db)),
        error
      );
    }
  }
}
