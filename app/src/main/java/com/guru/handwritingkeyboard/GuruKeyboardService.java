package com.guru.handwritingkeyboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class GuruKeyboardService extends InputMethodService {

    private Typeface guru;
    private EditText editor;

    @Override
    public View onCreateInputView() {

        guru = Typeface.createFromAsset(
                getAssets(),
                "GuruHandwriting.ttf"
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(12, 12, 12, 12);
        root.setBackgroundColor(Color.WHITE);

        editor = new EditText(this);
        editor.setHint("Type your message...");
        editor.setTextSize(22);
        editor.setTypeface(guru);
        editor.setGravity(Gravity.TOP);
        editor.setSingleLine(false);

        root.addView(
                editor,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);

        Button clear = new Button(this);
        clear.setText("Clear");

        clear.setOnClickListener(v ->
                editor.setText("")
        );

        Button create = new Button(this);
        create.setText("Create Handwriting");

        create.setOnClickListener(v ->
                createAndShareImage()
        );

        buttons.addView(
                clear,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                )
        );

        buttons.addView(
                create,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        2
                )
        );

        root.addView(buttons);

        return root;
    }

    private void createAndShareImage() {

        String text = editor.getText()
                .toString()
                .trim();

        if (text.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please type something first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {

            int width = 1200;
            int padding = 70;
            int lineHeight = 90;

            Paint paint = new Paint(
                    Paint.ANTI_ALIAS_FLAG
            );

            paint.setTypeface(guru);
            paint.setTextSize(58);
            paint.setColor(Color.BLACK);

            String[] lines =
                    text.split("\\n", -1);

            int height = Math.max(
                    180,
                    padding * 2 +
                    lines.length * lineHeight
            );

            Bitmap bitmap =
                    Bitmap.createBitmap(
                            width,
                            height,
                            Bitmap.Config.ARGB_8888
                    );

            Canvas canvas =
                    new Canvas(bitmap);

            canvas.drawColor(Color.WHITE);

            float y = padding + 58;

            for (String line : lines) {

                canvas.drawText(
                        line,
                        padding,
                        y,
                        paint
                );

                y += lineHeight;
            }

            File directory =
                    new File(
                            getCacheDir(),
                            "guru"
                    );

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File imageFile =
                    new File(
                            directory,
                            "guru_handwriting.png"
                    );

            FileOutputStream output =
                    new FileOutputStream(imageFile);

            bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    output
            );

            output.close();

            Uri imageUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName()
                                    + ".fileprovider",
                            imageFile
                    );

            Intent share =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            share.setType("image/png");

            share.putExtra(
                    Intent.EXTRA_STREAM,
                    imageUri
            );

            share.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(
                    Intent.createChooser(
                            share,
                            "Send Guru Handwriting"
                    )
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not create image: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
