package andrushkva.com.authorizationform;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MIN_NUM_CHARACTER_PASSWORD = 8;
    private static final int MAX_NUM_CHARACTER_PASSWORD = 12;
    private ArrayList<String> domains = new ArrayList<>();

    private LinearLayout back;
    private TextInputLayout mTextInputLayoutEmail;
    private AutoCompleteTextView mEditTextEmail;
    private TextInputLayout mTextInputLayoutPassword;
    private EditText mEditTextPassword;
    private Button mButtonRegistration;

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addDomains();
        initViews();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void addDomains() {
        domains.add("@mail.ru");
        domains.add("@gmail.com");
        domains.add("@hotmail.com");
        domains.add("@yahoo.com");
        domains.add("@outlook.com");
        domains.add("@adinet.com.uy");
        domains.add("@gmail.co.uk");
    }

    public void initViews() {
        back = findViewById(R.id.back);
        mTextInputLayoutEmail = findViewById(R.id.input_layout_email);
        mEditTextEmail = findViewById(R.id.edit_email);
        mTextInputLayoutPassword = findViewById(R.id.input_layout_password);
        mEditTextPassword = findViewById(R.id.edit_password);
        mButtonRegistration = findViewById(R.id.button_registration);

        mEditTextEmail.addTextChangedListener(getEmailTextWatcher());
        mEditTextPassword.addTextChangedListener(getPaswordTextWatcher());
        CustomFilterAdapter adapter = new CustomFilterAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1, domains);
        mEditTextEmail.setAdapter(adapter);

        back.setOnClickListener(this);
        mButtonRegistration.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                break;
            case R.id.button_registration:
                registration();
                break;
        }
    }

    public void registration() {
        if (isOnline(getApplicationContext())) {
            boolean isSendRequest = true;
            if (!checkEmailField()) {
                isSendRequest = false;
            } else if (!checkDomain(mEditTextEmail.getText().toString().split("@")[1])) {
                isSendRequest = false;
            }
            if (!checkPasswordField()) {
                isSendRequest = false;
            }
            if (isSendRequest) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            }
        } else {
            showAlertDialog(getResources().getString(R.string.no_internet));
        }
    }

    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ooops");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create().show();
    }

    public TextWatcher getEmailTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hideError(mTextInputLayoutEmail);
            }
        };
    }

    public TextWatcher getPaswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isCorrectNumberOfPasswordCharacters();
            }
        };
    }

    private void showError(TextInputLayout til, String text) {
        til.setError(text);
    }

    private void hideError(TextInputLayout til) {
        til.setError("");
    }

    public boolean checkEmailField() {
        if (isEmailFieldEmpty()) {
            return false;
        }
        if (!isValidEmail()) {
            return false;
        }
        return true;
    }

    public boolean checkPasswordField() {
        if (isPasswordFielDEmpty()) {
            return false;
        }
        if (!isCorrectNumberOfPasswordCharacters()) {
            return false;
        }
        return true;
    }

    public boolean isEmailFieldEmpty() {
        if (TextUtils.isEmpty(mEditTextEmail.getText().toString())) {
            showError(mTextInputLayoutEmail, getResources().getString(R.string.enter_email));
            return true;
        }
        return false;
    }

    public boolean isPasswordFielDEmpty() {
        if (TextUtils.isEmpty(mEditTextPassword.getText().toString())) {
            showError(mTextInputLayoutPassword, getResources().getString(R.string.enter_password));
            return true;
        }
        return false;
    }

    public boolean isValidEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(mEditTextEmail.getText().toString()).matches()) {
            showError(mTextInputLayoutEmail, getResources().getString(R.string.check_email_address));
            return false;
        }
        return true;
    }

    public boolean isCorrectNumberOfPasswordCharacters() {
        int numCharacters = mEditTextPassword.getText().toString().length();
        if (numCharacters < MIN_NUM_CHARACTER_PASSWORD) {
            showError(mTextInputLayoutPassword, getResources().getString(R.string.less_password));
            return false;
        } else if (numCharacters > MAX_NUM_CHARACTER_PASSWORD) {
            showError(mTextInputLayoutPassword, getResources().getString(R.string.longer_password));
            return false;
        } else {
            hideError(mTextInputLayoutPassword);
        }
        return true;
    }

    public boolean checkDomain(final String domain) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                boolean isSuccess = false;
                try {
                    byte[] hostAddress = InetAddress.getByName(domain).getAddress();
                    if (hostAddress.length > 0) {
                        isSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return isSuccess;
            }
        };
        Future<Boolean> future = executor.submit(callable);
        executor.shutdown();

        boolean isSuccess = false;
        try {
            isSuccess = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isSuccess) {
            showError(mTextInputLayoutEmail, getResources().getString(R.string.check_domain));
        }
        return isSuccess;
    }
}