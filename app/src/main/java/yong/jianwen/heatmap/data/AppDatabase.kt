package yong.jianwen.heatmap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import yong.jianwen.heatmap.data.dao.CarDao
import yong.jianwen.heatmap.data.dao.TrackDao
import yong.jianwen.heatmap.data.dao.TrackPointDao
import yong.jianwen.heatmap.data.dao.TrackSegmentDao
import yong.jianwen.heatmap.data.dao.TripDao
import yong.jianwen.heatmap.data.dao.TripWithTracksDao
import yong.jianwen.heatmap.data.entity.Car
import yong.jianwen.heatmap.data.entity.Track
import yong.jianwen.heatmap.data.entity.TrackPoint
import yong.jianwen.heatmap.data.entity.TrackSegment
import yong.jianwen.heatmap.data.entity.Trip

@Database(
    entities = [Car::class, Trip::class, Track::class, TrackSegment::class, TrackPoint::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun tripDao(): TripDao
    abstract fun trackDao(): TrackDao
    abstract fun trackSegmentDao(): TrackSegmentDao
    abstract fun trackPointDao(): TrackPointDao
    abstract fun tripWithTracksDao(): TripWithTracksDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "heatmap_database")
                    .createFromAsset("database/20250101_heatmap_database_v5.db")
                    /*.fallbackToDestructiveMigration()*/
                    .addMigrations(migration_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val migration_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE `track_temp` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trip_id` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `number` INTEGER NOT NULL,
                        `start` TEXT NOT NULL,
                        `end` TEXT NOT NULL,
                        `car_id` INT NOT NULL,
                        FOREIGN KEY(`trip_id`) REFERENCES `trip`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
                        FOREIGN KEY(`car_id`) REFERENCES `car`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
                    );
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO track_temp (id, trip_id, type, name, number, start, end, car_id)
                    SELECT id, trip_id, type, name, number, start, end, 1 FROM track
                """.trimIndent())

                db.execSQL("DROP TABLE track")
                db.execSQL("ALTER TABLE track_temp RENAME TO track")
            }
        }
    }
}