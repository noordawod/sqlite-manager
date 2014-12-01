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
and call `super()` where applicable (for both `onCreate()` and `onLowMemory()`.)

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
 * Just implement the SQLitePlan interface:
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
 * Or simply extend enclosed SQLiteUpgradePlan class:
 * Upgrade database version from V5 to V6.
 */
class V5toV6 extends SQLiteUpgradePlan {

  @Override
  public void applyUpgrade(final SQLiteDatabase db) {
  }
}
```

```java
/**
 * Upgrade database version from V6 to V7.
 */
class V6toV7 extends SQLiteUpgradePlan {

  @Override
  public void applyUpgrade(final SQLiteDatabase db) {
  }
}
```

Note: Downgrades work the same way, but in the opposite direction.

And now, you use the runner to properly upgrade from V4 to V7:

```java
class MyActivity extends Activity implements SQLitePlanRunner.Handler {

  private final static String LOG_TAG = "MyActivity";

  private final static String DB_NAME = "App.db";
  private final static int DB_VERSION = 7;

  private boolean isUpgrading;

  /**
   * Open the database when the app starts. If it needs upgrade, define the
   * upgrade plans and fire the runner. The handler is defined as well so
   * notifications regarding upgrade completion or errors will be dispatched to
   * our activity.
   */
  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    final SQLiteManager dbMgr = SQLiteManager.getInstance();

    SQLiteDatabase db = dbMgr.openDatabase(DB_NAME);
    if(dbMgr.needsUpgrade(db, DB_VERSION)) {
      // Signal that the database is being upgraded.
      isUpgrading = true;

      // TODO: This should be running on a different thread.
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
    Log.d(
      LOG_TAG, 
      "Schema changed successfully from V" + oldVersion + " to V" + newVersion
    );
  }

  @Override
  public void onChangeComplete(SQLiteDatabase db, int version) {
    // Callback fired when db is upgraded to target version.
    isUpgrading = false;
  }

  @Override
  public void onError(SQLiteDatabase db, Throwable error) {
    // Callback fired when db encountered an error.
    // TODO: Show an error to the user, ask what to do.
  }
}
```

The runner will wrap the upgrade execution within a transaction, so any error
happening during the upgrade will cause all changes to be ignored. If a handler
was defined, its `onError()` callback will be fired (as shown above.)

License
-------
This library is open source and released under the terms of MIT license.
You will find a LICENSE file in the library's root directory.
