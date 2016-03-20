/*
 * Pinnr
 *
 * Copyright (C) 2016 Dominik Fay
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
