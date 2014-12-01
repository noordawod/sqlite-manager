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

/**
 * Auxiliary class to initialize {@link SQLiteManager} properly. To use,
 * either point to this class in the manifest, or extend it in your app and
 * point to your class in the manifest.
 *
 * @see <a href="http://developer.android.com/guide/topics/manifest/application-element.html#nm">android:name property</a>
 * @since 1.0
 */
public class SQLiteApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Set application instance.
    SQLiteManager.getInstance().setApplication(SQLiteApplication.this);
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    SQLiteDatabase.releaseMemory();
  }
}
