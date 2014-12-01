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

package com.fine47.sqlite.aux;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Miscellaneous helper utilities.
 *
 * @since 1.0
 */
public class Util {

  private final static Pattern
    VALID = Pattern.compile("^[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)?$");

  /**
   * Normalizes a file name/path by checking whether it's a file name or an
   * absolute file path. The difference is quite simply determined by studying
   * the first character of the file: If it's a directory separator (normally
   * a '/' character) then it's a file path, otherwise it's a file name.
   *
   * For the later case, the file name is then transformed into an absolute file
   * path by calling {@link Application#getDatabasePath(java.lang.String)}
   * method.
   *
   * @param ctx execution context
   * @param filePath to normalize
   * @return normalized, absolute file path
   */
  public static String normalizeFilePath(Context ctx, String filePath) {
    // If file path doesn't start with a directory separator, we need to create
    // it under the app's database directory.
    if(!filePath.startsWith(File.separator)) {
      // The file name must include only allowed characters.
      if(!VALID.matcher(filePath).matches()) {
        throw new IllegalArgumentException(
          "File name is invalid: " + filePath);
      }

      // Get full path of this database from app's context.
      filePath = ctx.getDatabasePath(filePath).getAbsolutePath();
    }

    return filePath;
  }

  /**
   * Returns the parent path for the specified file path. This is normally the
   * path to the containing directory.
   *
   * @param filePath to get parent path for
   * @return parent path
   */
  public static String getParentPath(String filePath) {
    int lastPos = filePath.lastIndexOf(File.separatorChar);
    return 0 > lastPos ? filePath : filePath.substring(0, lastPos);
  }

  /**
   * Returns the name of the {@link File} that's associated with the specified
   * database instance.
   *
   * @param db database instance
   * @return database file name
   */
  public static String getFileName(SQLiteDatabase db) {
    assert null != db;
    final String absolutePath = db.getPath();
    final int lastSlashPos = absolutePath.lastIndexOf(File.separatorChar);
    return 0 > lastSlashPos
      ? absolutePath
      : absolutePath.substring(1 + lastSlashPos);
  }
}
