package de.faysapps.pinnr;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.Set;

public class WordsExtractor {

    private static final String TABLE_NAME = "n2w";
    private static final String WORD_COLUMN_NAME = "word";

    private final SQLiteDatabase db;

    public WordsExtractor(SQLiteDatabase db) {
        this.db = db;
    }

    public Set<String> extract(long pin) {
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{WORD_COLUMN_NAME},
                "num = " + pin,
                null, null, null, null);
        Set<String> words = new HashSet<>();
        for (boolean next = cursor.moveToFirst(); next; next = cursor.moveToNext()) {
            words.add(cursor.getString(0));
        }
        cursor.close();
        return words;
    }
}
