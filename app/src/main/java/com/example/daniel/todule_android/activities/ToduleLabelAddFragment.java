package com.example.daniel.todule_android.activities;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.todule_android.R;
import com.example.daniel.todule_android.provider.ToduleDBContract;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;

/**
 * Created by danieL on 10/20/2017.
 */

public class ToduleLabelAddFragment extends Fragment {
    MainActivity myActivity;
    ColorPickerView cpView;
    TextView preview_text;
    EditText tag_edit;
    Integer selected_color;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myActivity = (MainActivity) getActivity();
        myActivity.getSupportActionBar().setTitle("New label");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_label_add, container, false);

        if (savedInstanceState != null){
            selected_color = savedInstanceState.getInt("selected_color");
        }
        tag_edit = view.findViewById(R.id.tag_edit);
        preview_text = view.findViewById(R.id.preview_label);

        tag_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                preview_text.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        cpView = view.findViewById(R.id.color_picker_view);

        if(selected_color == null){
            // Set default
            selected_color = ContextCompat.getColor(getContext(), R.color.normalGreen);
        }
        cpView.setColor(selected_color, false);
        preview_text.setBackgroundColor(selected_color);

        cpView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                tag_edit.clearFocus();
                myActivity.hideSoftKeyboard(true);
                selected_color = i;
                preview_text.setBackgroundColor(selected_color);
                Bitmap image = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888);
                image.eraseColor(i);
                Palette.from(image).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        Palette.Swatch vibrant = palette.getVibrantSwatch();
                        if (vibrant != null) {
                            preview_text.setTextColor(vibrant.getTitleTextColor());
                        }
                    }
                });

            }
        });

        tag_edit.requestFocus();
        tag_edit.postDelayed(new Runnable() {
            @Override
            public void run() {
                myActivity.hideSoftKeyboard(false);
            }
        }, 200);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(selected_color != null) {
            outState.putInt("selected_color", selected_color);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_fragment_label_add, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                ContentValues cv = new ContentValues();
                cv.put(ToduleDBContract.TodoLabel.COLUMN_NAME_TAG, preview_text.getText().toString());
                cv.put(ToduleDBContract.TodoLabel.COLUMN_NAME_COLOR, cpView.getSelectedColor());
                cv.put(ToduleDBContract.TodoLabel.COLUMN_NAME_TEXT_COLOR, preview_text.getCurrentTextColor());
                Uri itemUri = getContext().getContentResolver().insert(ToduleDBContract.TodoLabel.CONTENT_URI, cv);
                Toast.makeText(getContext(), "Label '" + preview_text.getText().toString() + "' created", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}