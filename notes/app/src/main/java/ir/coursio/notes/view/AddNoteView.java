package ir.coursio.notes.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;

import ir.coursio.notes.R;
import ir.coursio.notes.model.structures.NoteStruct;
import ir.coursio.notes.presenter.AddNotePresenter;
import ir.coursio.notes.util.TextStyleHandler;

/**
 * Created by Taher on 30/05/2017.
 * Project: notes
 */

@SuppressLint("ViewConstructor")
public class AddNoteView extends FrameLayout implements View.OnClickListener {
    //Note title and text EditText
    private EditText edtText, edtTitle;
    private AddNotePresenter presenter;
    private ViewGroup mainLayout;

    /**
     * Enum type that indicates the status of clicked item.
     */
    private enum ClickedActionItem {
        BOLD, ITALIC, CLEAR
    }

    public AddNoteView(@NonNull final Activity activity) {
        super(activity);
        View view = inflate(getContext(), R.layout.activity_add_note, this);


        edtTitle = (EditText) view.findViewById(R.id.edtTitle);
        edtText = (EditText) view.findViewById(R.id.edtText);
        mainLayout = (ViewGroup) view.findViewById(R.id.mainLayout);


        // Action item click listeners setup
        ImageView imgBold = (ImageView) view.findViewById(R.id.imgBold);
        ImageView imgItalic = (ImageView) view.findViewById(R.id.imgItalic);
        ImageView imgClear = (ImageView) view.findViewById(R.id.imgClear);
        imgBold.setOnClickListener(this);
        imgItalic.setOnClickListener(this);
        imgClear.setOnClickListener(this);
        imgBold.setTag(ClickedActionItem.BOLD);
        imgItalic.setTag(ClickedActionItem.ITALIC);
        imgClear.setTag(ClickedActionItem.CLEAR);

        // Toolbar setup
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.add_note_toolbar);
        MenuItem save = toolbar.getMenu().findItem(R.id.save);
        toolbar.setNavigationIcon(getIcon(R.drawable.ic_arrow_back_24dp));
        save.setIcon(getIcon(R.drawable.ic_check_24dp));
        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (edtText.getText().toString().equals("")) {
                    showMessage(getContext().getString(R.string.message_no_text_warning));
                } else {
                    ((OnSaveListener) presenter).onSave(edtTitle.getText().toString(), edtText.getText());
                }
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
    }

    public void setPresenter(AddNotePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onClick(View v) {
        final TextStyleHandler styleHandler = new TextStyleHandler(getContext());

        switch ((ClickedActionItem) v.getTag()) {
            case BOLD:
                styleHandler.applyStyleToText(edtText, android.graphics.Typeface.BOLD);
                break;
            case ITALIC:
                styleHandler.applyStyleToText(edtText, Typeface.ITALIC);
                break;
            case CLEAR:
                edtText.setText("");
                break;
        }
    }

    /**
     * If the viewing note is not a new note we need to retrieve user data.
     * This method gets data from AddNoteModel and set them on current views.
     *
     * @param note The NoteStruct which contains user's saved data.
     */
    public void editMode(NoteStruct note) {
        edtTitle.setText(note.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            edtText.setText(Html.fromHtml(note.getText(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            //noinspection deprecation
            edtText.setText(Html.fromHtml(note.getText()));
        }
    }

    /**
     * Show a message to user using Snackbar.
     *
     * @param message The String message to show to user.
     */
    private void showMessage(String message) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private Drawable getIcon(int id) {
        return ContextCompat.getDrawable(getContext(), id);
    }

    public interface OnSaveListener {
        void onSave(String title, Editable text);
    }
}
