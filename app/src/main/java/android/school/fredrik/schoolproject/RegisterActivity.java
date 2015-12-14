package android.school.fredrik.schoolproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * Handles the registration functionality.
 * @author Fredrik Johansson
 */
public class RegisterActivity extends AppCompatActivity{

    // xxx
    private User user = User.getINSTANCE();

    // xxx
    private ClientSideValidation validator = null;

    // UI references.
    private EditText eMailView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;

    // LOG TAG
    private static final String TAG = RegisterActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Start of onCreate method.");

        setContentView(R.layout.activity_register);

        // So that we can navigate back to the login activity with the up arrow.
        setupActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Instantiates UI elements.
        eMailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        // When the user clicks the registration button. A attempt will be made to log register the user.
        mEmailSignInButton.setOnClickListener(
                view -> attemptRegistration()
        );

        Log.d(TAG, "End of onCreate method.");
    }

    /**
     * Enables the up button in the menu.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Called from view. Attempts to register. Does client-side validation before sending user info to the server for server-side validation.
     * */
    private void attemptRegistration() {

        // Reset errors.
        eMailView.setError(null);
        passwordView.setError(null);

        // Get user supplied values.
        String email = eMailView.getText().toString();
        String password = passwordView.getText().toString();

        // xxx
        View focusView = null;

        validator = new ClientSideValidation(this);

        validator.checkValidity(email, password);
        boolean success = validator.getSuccess();

        // If client-side validation failed.
        if(!success){
            String message = validator.getMessage();

            if(message.equals(getString(R.string.error_invalid_password))){
                passwordView.setError(message);
                focusView = passwordView;
            }

            else if(message.equals(getString(R.string.error_field_required))){
                eMailView.setError(message);
                focusView = eMailView;
            }

            else if(message.equals(getString(R.string.error_invalid_email))){
                eMailView.setError(message);
                focusView = eMailView;
            }

            /* There was an error; don't attempt login and focus the first
            form field with an error.*/
            focusView.requestFocus();
        }

        else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registration attempt.
            showProgress(true);
            new UserRegisterTask(email, password, this).execute((Void) null);;
        }
    }

    /**
     * Note from Fredrik: I don't fully understand this code.. but it works.
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // THIS DOES NOT WORK RIGHT NOW (WIP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(TAG, "Tried to navigate back");

                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);

                // finish();
                // NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This task is starting the server-side validation.
     * It makes a call to the server (using the User-class)
     * with the user info it got through the constructor.
     *
     * @author Fredrik Johansson
     * */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final Context context;

        // The task get supplied the user supplied information in the constructor.
        UserRegisterTask(String email, String password, Context context) {
            mEmail = email;
            mPassword = password;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Using the user supplied information to make a login attempt.
            return user.register(mEmail, mPassword, context);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);

            // If the server-side validation succeeded.
            if (success) {
                // The user gets sent to the LoginActivity. (due to the success of the registration)
                Intent intent = new Intent(RegisterActivity.this.getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }

            // If the server-side validation failed.
            else {
                // The user gets notified about the failure of the registration attempt.
                passwordView.setError(getString(R.string.server_error));
                passwordView.requestFocus();
            }
        }


    }
}

