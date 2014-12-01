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

/**
 * Applies changes to an {@link SQLiteDatabase} schema and either upgrades or
 * downgrades to another version.
 *
 * @since 1.0
 */
public interface SQLitePlan {

  /**
   * Applies an upgrade to the specified {@link SQLiteDatabase} from a previous
   * version to the version of this {@link SQLitePlan}. The changes will be
   * executed within a transaction; any and all errors will cause changes to be
   * rolled back.
   *
   * @param db to apply the upgrade to
   */
  public void applyUpgrade(final SQLiteDatabase db);

  /**
   * Applies an downgrade to the specified {@link SQLiteDatabase} from a newer
   * version to the version of this {@link SQLitePlan}. The changes will be
   * executed within a transaction; any and all errors will cause changes to be
   * rolled back.
   *
   * @param db to apply the downgrade to
   */
  public void applyDowngrade(final SQLiteDatabase db);
}
