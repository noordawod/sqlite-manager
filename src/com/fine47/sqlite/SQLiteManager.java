/**
 * This file is part of SQLite Manager library.
 * Copyright (C) 2014 Noor Dawod. All rights reserved.
 * https://github.com/noordawod/sqlite-manager
 *
 * Released under the MIT license
 * http://en.wikipedia.org/wiki/MIT_License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.fine47.sqlite;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.fine47.sqlite.aux.Util;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all {@link SQLiteDatabase} instances and handles low-memory
 * situations for all instances. To allow the manager to perform its duties,
 * it must obtain a reference to the running {@link Application}
 * by calling {@link #setApplication(Application)}.
 *
 * To simplify, an integrated {@link SQLiteApplication} class can be used to
 * properly initialize this manager.
 *
 * @see SQLiteApplication
 * @since 1.0
 */
public class SQLiteManager {

  /**
   * Logging tag for this class.
   */
  public final static String LOG_TAG = "SQLiteManager";

  private final static ConcurrentHashMap<String, SQLiteDatabase>
    databases = new ConcurrentHashMap();

  private static SQLiteManager instance;

  private Application app;

  private SQLiteManager() {
    // Singleton.
  }

  /**
   * Retrieves the singleton instance of this class.
   *
   * @return singleton instance
   */
  public static SQLiteManager getInstance() {
    if(null == instance) {
      instance = new SQLiteManager();
    }
    return instance;
  }

  /**
   * Returns the attached {@link Application} instance.
   *
   * @return current {@link Application} instance
   */
  public Application getApplication() {
    if(null == app) {
      throw new IllegalStateException(
        "Application context has not been defined yet.");
    }
    return app;
  }

  /**
   * Attaches the running {@link Application} instance.
   * For default setups, just use the supplied {@link SQLiteApplication} class
   * to automatically set this up.
   *
   * @see SQLiteApplication
   * @param app the {@link Application} instance
   */
  public void setApplication(Application app) {
    if(null == app) {
      throw new IllegalStateException(
        "Trying to set an empty application context.");
    }
    this.app = app;
  }

  /**
   * Returns {@link SQLiteDatabase} instance for the specified file.
   * No {@link CursorFactory} will be used.
   *
   * @param file of database file
   * @return database instance
   */
  public SQLiteDatabase openDatabase(File file) {
    return openDatabase(file, null);
  }

  /**
   * Returns {@link SQLiteDatabase} instance for the specified file and use the
   * specified {@link CursorFactory} as the cursor.
   *
   * @param file of database file
   * @param cursor to use with the database
   * @return database instance
   */
  public SQLiteDatabase openDatabase(File file, CursorFactory cursor) {
    return openDatabase(file.getAbsolutePath(), cursor);
  }

  /**
   * Returns {@link SQLiteDatabase} instance for the specified file path.
   * No {@link CursorFactory} will be used.
   *
   * @param filePath file name/path of database file
   * @return database instance
   */
  public SQLiteDatabase openDatabase(String filePath) {
    return openDatabase(filePath, null);
  }

  /**
   * Returns {@link SQLiteDatabase} instance for the specified file path and use
   * the specified {@link CursorFactory} as the cursor. If the file path is
   * actually a file name, an absolute file path will be generated using
   * {@link Util#normalizeFilePath(android.content.Context, java.lang.String)}
   * method.
   *
   * @param filePath file name/path of database file
   * @param cursor to use with the database
   * @return {@link SQLiteDatabase} instance
   */
  public SQLiteDatabase openDatabase(String filePath, CursorFactory cursor) {
    // Normalize the file path if application is known.
    filePath = Util.normalizeFilePath(getApplication(), filePath);

    // Try to find a cached instance.
    SQLiteDatabase db = databases.get(filePath);

    // If this is the first time for getting this database, or if the database
    // was previously closed by the user.
    if(null == db || !db.isOpen()) {
      // Auto-create parent directories.
      File parentFile = new File(Util.getParentPath(filePath));

      // If parent directory is actually a file, throw an error.
      if(parentFile.isFile()) {
        throw new IllegalStateException(
          "Parent directory is actually a file: " + parentFile);
      }

      // Auto-create parent directories.
      if(!parentFile.isDirectory() && !parentFile.mkdirs()) {
        throw new IllegalStateException(
          "Unable to create parent directories for database: " + filePath);
      }

      try {
        // Try to open the database first.
        db = SQLiteDatabase.openDatabase(
          filePath,
          cursor,
          SQLiteDatabase.OPEN_READWRITE
        );
      } catch(SQLiteDatabaseCorruptException error) {
        Log.e(LOG_TAG, "Corruption error detected in database: " + filePath);
        throw error;
      } catch(SQLiteException ignored) {
        // The database is possibly non-existent, create it.
        db = SQLiteDatabase.openOrCreateDatabase(filePath, cursor);

        // Database created; set initial version.
        db.setVersion(1);
      }

      // Store in internal dictionary.
      databases.put(filePath, db);
    }

    return db;
  }

  /**
   * Checks whether the specified {@link SQLiteDatabase} has a version which
   * is older than the specified version, and thus is eligible for upgrade.
   *
   * @param db to check
   * @param targetVersion new version of database to check against
   * @return TRUE if database needs upgrade, FALSE otherwise
   */
  public boolean needsUpgrade(SQLiteDatabase db, int targetVersion) {
    return targetVersion > db.getVersion();
  }

  /**
   * Checks whether the specified {@link SQLiteDatabase} has a version which
   * is newer than the specified version, and thus is eligible for downgrade.
   *
   * @param db to check
   * @param targetVersion old version of database to check against
   * @return TRUE if database needs downgrade, FALSE otherwise
   */
  public boolean needsDowngrade(SQLiteDatabase db, int targetVersion) {
    return targetVersion < db.getVersion();
  }
}
