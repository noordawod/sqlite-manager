SQLite Manager
==============

This library sweetens your migrations of SQLite databases.

Features include:

* Uses the Application's context directly (no need to supply Context anymore);
* A simple yet powerful schema upgrade/downgrade plan;
* A simple handler to manage progress and completion of the plans.

Show me the code
----------------

For the lazy, just point to the attached Application wrapper in your manifest:

```xml
<?xml version="1.0" encoding="utf-8"?>

<manifest
  xmlns:android="http://schemas.android.com/apk/res/android"
  ...
>

  <application
    android:name="com.fine47.sqlite.SQLiteApplication"
    ...
  />

</manifest>
```

And then use SQLiteManager's methods to open a database (always in read-write,
and it will auto-create if non-existent), check versions, check for upgrade or
downgrade and much more.

If you already use your own Application wrapper, just extend the enclosed one
and call `super()` where application (for both `onCreate()` and 
`onLowMemory()`.)

How easy it is to upgrade?
--------------------------

There are two parts for an upgrade:

* Define plans to upgrade from an old version to a newer one
* Use the enclosed runner (implements `Runnable`) to upgrade

The plans simply need to implement `SQLitePlan` interface. For simplicity's
sake, there are two abstract classes which target upgrades (`SQLiteUpgradePlan`)
and downgrades (`SQLiteDowngradePlan`), so you can just extend those.

Let's say your app is installed in an old device and has a database with V4. 
During the last few weeks, you were busy at work in upgrading and making changes
so the database version is now V7.

This means you need to define plans for upgrading from V4 to V5, from V5 to V6
and finally from V6 to V7. Obviously, you should have also all the plans for
upgrading from earlier versions -- you don't know which version the app has.

Let's see how to define plans:

```java
/**
 * Upgrade database version from V4 to V5.
 */
class V4toV5 implements SQLitePlan {

  @Override
  public void applyUpgrade(final SQLiteDatabase db) {
  }

  @Override
  public void applyDowngrade(final SQLiteDatabase db) {
  }
}
```

```java
/**
 * Upgrade database version from V5 to V6.
 */
class V5toV6 implements SQLitePlan {

  @Override
  public void applyUpgrade(final SQLiteDatabase db) {
  }

  @Override
  public void applyDowngrade(final SQLiteDatabase db) {
  }
}
```

```java
/**
 * Upgrade database version from V6 to V7.
 */
class V6toV7 implements SQLitePlan {

  @Override
  public void applyUpgrade(final SQLiteDatabase db) {
  }

  @Override
  public void applyDowngrade(final SQLiteDatabase db) {
  }
}
```

And now, you use the runner to properly upgrade from V4 to V7:

```java
/**
 * Upgrade database version from V6 to V7.
 */
class MyActivity extends Activity implements SQLitePlanRunner.Handler {

  private final static String DB_NAME = "App.db";
  private final static int DB_VERSION = 7;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    final dbMgr = SQLiteManager.getInstance();

    SQLiteDatabase db = dbMgr.openDatabase(DB_NAME);
    if(dbMgr.needsUpgrade(db, DB_VERSION)) {
      // This should be running on a different thread, for simplicity's sake
      // it's inline.
      SQLitePlanRunner
        .For(db, DB_VERSION)
        .setHandler(this)
        .addPlan(1, V1toV2.class)
        .addPlan(2, V2toV3.class)
        .addPlan(3, V3toV4.class)
        .addPlan(4, V4toV5.class)
        .addPlan(5, V5toV6.class)
        .addPlan(6, V6toV7.class)
        .run();
    }
  }

  @Override
  public void onChange(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Callback fired when db is upgraded from oldVersion to newVersion.
  }

  @Override
  public void onChangeComplete(SQLiteDatabase db, int version) {
    // Callback fired when db is upgraded to target version.
  }

  @Override
  public void onError(SQLiteDatabase db, Throwable error) {
    // Callback fired when db encountered an error.
  }
}
```

License
-------
This library is open source and is released under the terms of MIT license. You
should find a LICENSE file enclosed in the ZIP file or git checkout.
