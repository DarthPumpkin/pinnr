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

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.common.base.Joiner;

import java.util.Set;


public class MainActivity extends Activity {

    private EditText codeET;
    private TextView wordsTV;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button1);
        wordsTV = (TextView) findViewById(R.id.realWordsTV);
        codeET = (EditText) findViewById(R.id.editText1);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        wordsTV.setMovementMethod(new ScrollingMovementMethod());

        SQLiteDatabase db = new MyDatabase(this).getReadableDatabase();
        final WordsExtractor extractor = new WordsExtractor(db);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long pin = Long.parseLong(codeET.getText().toString());
                Set<String> words = extractor.extract(pin);
                String joinedText = Joiner.on(", ").join(words);
                wordsTV.setText(joinedText);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
