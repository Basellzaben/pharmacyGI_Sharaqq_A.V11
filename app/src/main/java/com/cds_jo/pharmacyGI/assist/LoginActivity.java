package com.cds_jo.pharmacyGI.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.Authenticator;

import com.cds_jo.pharmacyGI.GalaxyMainActivity;
import com.cds_jo.pharmacyGI.Main2Activity;

import com.cds_jo.pharmacyGI.R;
import com.cds_jo.pharmacyGI.SqlHandler;
import com.cds_jo.pharmacyGI.We_Result;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.Transport;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEmailView.setText(sharedPreferences.getString("UserID", ""));

        this.setTitle(sharedPreferences.getString("CompanyNm", ""));
        //
        populateAutoComplete();


        SharedPreferences.Editor editor    = sharedPreferences.edit();
        editor.putString("Login", "No");
        editor.commit();



        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
       /* else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
           // showProgress(true);
         //   mAuthTask = new UserLoginTask(email, password);




            if(email.toString().equals("-1") && password.toString().equals("")) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor    = sharedPreferences.edit();
                editor.putString("UserName","Administrator System ");
                editor.putString("UserID", "-1");
                editor.putString("Login", "Yes");
                editor.commit();
                Intent k = new Intent(LoginActivity.this,GalaxyMainActivity.class);
                startActivity(k);
            }

            if(email.toString().equals("admin") && (password.toString().equals("") || (password.toString().equals("1" ))) ){
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor    = sharedPreferences.edit();
                editor.putString("UserName","Administrator System ");
                editor.putString("UserID", "-1");
                editor.putString("Login", "Yes");
                editor.commit();
                Intent k = new Intent(LoginActivity.this,GalaxyMainActivity.class);
                 startActivity(k);
            }

            SqlHandler sqlHandler = new SqlHandler(this);

            String query = "SELECT  name   from  manf where man = '"+email.toString()+"' And password='"+password.toString()+"'";
            Cursor c1 = sqlHandler.selectQuery(query);


            if (c1.getCount() > 0 ) {
                c1.moveToFirst();
                mPasswordView.setText("");

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor    = sharedPreferences.edit();
                editor.putString("UserName", String.valueOf(c1.getString(0)));
                editor.putString("UserID", email.toString());
                editor.putString("Login", "Yes");
                editor.commit();





              /*   SharedPreferences sharedPref = this.getPreferences(this.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("UserName", "String.valueOf(c1.getString(0))");
                editor.commit();*/
                //Intent k = new Intent(LoginActivity.this,MasterActivity.class);
                Intent k = new Intent(LoginActivity.this,Main2Activity.class);

                // k.putExtra("UserName", String.valueOf(c1.getString(0)) );
                startActivity(k);
                c1.close();
                return;
            }else
            {
                Toast.makeText(this,"معلومات الدخول غير صحيحة",Toast.LENGTH_SHORT).show();
                return;
            }






           // mAuthTask.execute((Void) null);
        }


    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public void BtnSkip(View view) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor    = sharedPreferences.edit();
        editor.putString("UserName", "Test");
        editor.putString("UserID","26");
        editor.putString("Login", "Yes");
        editor.commit();

        Intent k = new Intent(LoginActivity.this,GalaxyMainActivity.class);
        startActivity(k);
    }

    public void btn_GetManf(View view) {


        onProgressUpdate_Items();




    }

    public void onProgressUpdate_Items(){
        final Handler _handler = new Handler();
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("الرجاء الانتظار");
        progressDialog.setMessage("العمل جاري على نسخ البيانات");
        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CallWebServices ws = new CallWebServices(LoginActivity.this);
                ws.GetManf();
                try {

                    Integer i;
                    String q;
                    JSONObject js = new JSONObject(We_Result.Msg);
                    JSONArray js_man= js.getJSONArray("man");
                    JSONArray js_name= js.getJSONArray("name");
                    JSONArray js_MEName= js.getJSONArray("MEName");
                    JSONArray js_StoreNo= js.getJSONArray("StoreNo");
                    JSONArray js_Stoped= js.getJSONArray("Stoped");
                    JSONArray js_SupNo= js.getJSONArray("SupNo");
                    JSONArray js_UserName= js.getJSONArray("UserName");
                    JSONArray js_Password= js.getJSONArray("Password");


                    SqlHandler sqlHandler = new SqlHandler(LoginActivity.this);
                    q = "Delete from manf";
                    sqlHandler.executeQuery(q);
                    for (i = 0; i < js_man.length(); i++) {
                        q = "Insert INTO manf(man,name,MEName,username,password,StoreNo,Stoped,SupNo) values ("
                                +         js_man.get(i).toString()
                                + ",'" + js_name.get(i).toString()
                                + "','" + js_MEName.get(i).toString()
                                + "','" + js_UserName.get(i).toString()
                                + "','" + js_Password.get(i).toString()
                                + "','" + js_StoreNo.get(i).toString()
                                + "'," + js_Stoped.get(i).toString()
                                + "," + js_SupNo.get(i).toString()
                                + ")";
                        sqlHandler.executeQuery(q);
                        progressDialog.setMax(js_man.length());
                        progressDialog.incrementProgressBy(1);

                        if (progressDialog.getProgress() == progressDialog.getMax()) {
                            progressDialog.dismiss();
                        }
                    }
                    final int total = i;
                    _handler.post(new Runnable() {
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    LoginActivity.this).create();
                            alertDialog.setTitle("تحديث البيانات");
                            alertDialog.setMessage("تمت عملية تحديث البيانات بنجاح" + "   " + String.valueOf(total));
                            alertDialog.setIcon(R.drawable.tick);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();

                            progressDialog.dismiss();
                        }
                    });
                } catch (final Exception e) {
                    progressDialog.dismiss();
                    _handler.post(new Runnable() {
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(
                                    LoginActivity.this).create();
                            alertDialog.setTitle("المستخدمين");
                            if (We_Result.ID==-404){
                                alertDialog.setMessage( We_Result.Msg);
                            }
                            else  {
                                alertDialog.setMessage("لا يوجد بيانات" +  We_Result.ID + "");
                            }
                            alertDialog.setIcon(R.drawable.tick);
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }
            }
        }).start();



    }


    public static void sendEmailWithAttachments(String host, String port,
                                                final String userName, final String password, String toAddress,
                                                String subject, String message, String[] attachFiles)
            throws AddressException, MessagingException {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(   userName,   password);
            }
        };
       Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
       // messageBodyPart.setContent(message, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // adds attachments
       /* if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                 try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                multipart.addBodyPart(attachPart);
            }
        }
*/
        // sets the multi-part as e-mail's content
      msg.setContent(multipart);

        // sends the e-mail
        Transport.send(msg);

    }

























                   private void sendMail(String email, String subject, String messageBody) {
                          Session session = createSessionObject();

                          try {
                              Message message = createMessage("maen.naamneh@yahoo.com", subject, messageBody, session);
                              new SendMailTask().execute(message);
                          } catch (AddressException e) {
                              e.printStackTrace();
                          } catch (MessagingException e) {
                              e.printStackTrace();
                          } catch (UnsupportedEncodingException e) {
                              e.printStackTrace();
                          }
                      }



                  private Session createSessionObject() {
                          Properties properties = new Properties();
                          properties.put("mail.smtp.auth", "true");
                          properties.put("mail.smtp.starttls.enable", "true");
                          properties.put("mail.smtp.host", "smtp.gmail.com");
                          properties.put("mail.smtp.port", "587");

                          return Session.getInstance(properties, new javax.mail.Authenticator() {
                              protected PasswordAuthentication getPasswordAuthentication() {
                                  return new PasswordAuthentication("zain.m.naamneh@gmail.com","zain12345");
                              }
                          });
                      }





                                private Message createMessage(String email, String subject, String messageBody, Session session) throws
                                           MessagingException, UnsupportedEncodingException {
                                       Message message = new MimeMessage(session);
                                       message.setFrom(new InternetAddress("zain.m.naamneh", "zain12345"));
                                       message.addRecipient(Message.RecipientType.TO, new InternetAddress("maen.naamneh@yahoo.com", "mae12n.naamneh@yahoo.com"));
                                       message.setSubject(subject);
                                       //message.setContent(subject, "text; charset=utf-8");
                                       return message;
                                   }



                                           public class SendMailTask extends AsyncTask<Message, Void, Void> {
                                               private ProgressDialog progressDialog;

                                               @Override
                                               protected void onPreExecute() {
                                                   super.onPreExecute();
                                                   progressDialog = ProgressDialog.show(LoginActivity.this, "Please wait", "Sending mail", true, false);
                                               }

                                               @Override
                                               protected void onPostExecute(Void aVoid) {
                                                   super.onPostExecute(aVoid);
                                                   progressDialog.dismiss();
                                               }

                                               protected Void doInBackground(javax.mail.Message... messages) {
                                                   try {

                                                        Transport.send(messages[0]);
                                                   } catch (MessagingException e) {
                                                       e.printStackTrace();
                                                   }
                                                   return null;
                                               }

                                           }




    public void sendEmail(View view) {

        final String username = "maen.naamneh@yahoo.com";
        final String password = "2005974033";

          //   sendMail("emailid", "subject", "message");


      GMailSender sender = new GMailSender("zain.m.naamneh@gmail.com", "zain12345");


    /*    try {*/
    /*        sender.sendMail("My Subject That cannot be changed",*/
    /*                "My Body That cannot be changed",*/
    /*                "zain.m.naamneh@gmail.com",*/
    /*                "maen.naamneh@yahoo.com");*/
    /*    }*/
    /*    catch ( Exception ex)      {}*/
                              /* Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("maen.naamneh@yahoo.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("eng_naamneh@yahoo.com"));
            message.setSubject("Testing Subject");
            //message.setText("PFA");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            String file = "path of file to be attached";
            String fileName = "attachmentName";
           // DataSource source = new FileDataSource(file);
           // messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

          //  message.setContent(multipart);

            System.out.println("Sending");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }*/


/*
        String host = "smtp.gmail.com";
        String port = "587";
        String mailFrom = "maen.naamneh@yahoo.com";
        String password = "2005974033";

        // message info
        String mailTo = "eng_naamneh@yahoo.com";
        String subject = "New email with attachments";
        String message = "I have some attachments for you.";

        // attachments
        String[] attachFiles = new String[3];
        attachFiles[0] = "e:/Test/Picture.png";
        attachFiles[1] = "e:/Test/Music.mp3";
        attachFiles[2] = "e:/Test/Video.mp4";

        try {
            sendEmailWithAttachments(host, port, mailFrom, password, mailTo,
                    subject, message, attachFiles);
            System.out.println("Email sent.");
        } catch (Exception ex) {
            System.out.println("Could not send email.");
            ex.printStackTrace();
        }*/
    }





    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

 /*  @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
       // finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    @Override
    public void onDestroy()
    {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
}

