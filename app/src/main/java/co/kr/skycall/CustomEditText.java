package co.kr.skycall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * @author Diana Prața
 *         Date: 5/29/2015.
 */
public class CustomEditText extends AppCompatEditText {
    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}