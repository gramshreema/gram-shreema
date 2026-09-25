package com.shreema.gramshikayat;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
import android.view.View;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    LinearLayout body;
    EditText phone, otp, name, details, location;
    Spinner category;
    TextView status;

    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            home();
        } else {
            login();
        }
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setPadding(15, 15, 15, 15);
        return t;
    }

    Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        return b;
    }

    void screen(String title) {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);

        TextView heading =
                text("🌿 ग्राम श्रीमा\n" + title, 24);

        heading.setTextColor(Color.WHITE);
        heading.setBackgroundColor(
                Color.rgb(8, 127, 67));

        root.addView(heading);

        ScrollView scroll = new ScrollView(this);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(body);
        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1));

        setContentView(root);
    }

    void login() {

        screen("मोबाइल OTP लॉगिन");

        phone = new EditText(this);
        phone.setHint("10 अंकों का मोबाइल नंबर");
        phone.setInputType(3);
        body.addView(phone);

        Button send = button("OTP भेजें");
        body.addView(send);

        otp = new EditText(this);
        otp.setHint("OTP डालें");
        otp.setInputType(2);
        body.addView(otp);

        Button verify =
                button("OTP सत्यापित करें");

        body.addView(verify);

        status = text("", 16);
        body.addView(status);

        send.setOnClickListener(v -> {

            String number =
                    phone.getText()
                         .toString()
                         .trim();

            if (number.length() != 10) {
                status.setText(
                        "सही मोबाइल नंबर डालें");
                return;
            }

            PhoneAuthOptions options =
                    PhoneAuthOptions
                    .newBuilder(auth)
                    .setPhoneNumber("+91" + number)
                    .setTimeout(
                            60L,
                            java.util.concurrent.TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(
                            new PhoneAuthProvider
                            .OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(
                                PhoneAuthCredential credential) {

                            auth.signInWithCredential(
                                    credential)
                                .addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) {
                                        home();
                                    }
                                });
                        }

                        @Override
                        public void onVerificationFailed(
                                FirebaseException e) {

                            status.setText(
                                    "OTP नहीं भेजा गया:\n" +
                                    e.getMessage());
                        }

                        @Override
                        public void onCodeSent(
                                String id,
                                PhoneAuthProvider
                                .ForceResendingToken token) {

                            verificationId = id;

                            status.setText(
                                    "OTP भेज दिया गया है।");
                        }
                    })
                    .build();

            PhoneAuthProvider
                    .verifyPhoneNumber(options);
        });

        verify.setOnClickListener(v -> {

            if (verificationId == null) {
                status.setText(
                        "पहले OTP भेजें");
                return;
            }

            String code =
                    otp.getText()
                       .toString()
                       .trim();

            PhoneAuthCredential credential =
                    PhoneAuthProvider
                    .getCredential(
                            verificationId,
                            code);

            auth.signInWithCredential(
                    credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        home();
                    } else {
                        status.setText(
                                "OTP गलत है");
                    }
                });
        });
    }

    void home() {

        screen("जन शिकायत एवं समाधान");

        body.addView(
                text(
                        "आपकी समस्या, हमारी जिम्मेदारी",
                        20));

        Button complaint =
                button("📝 शिकायत दर्ज करें");

        Button track =
                button("🔎 शिकायत की स्थिति");

        Button mine =
                button("📋 मेरी शिकायतें");

        Button logout =
                button("लॉगआउट");

        body.addView(complaint);
        body.addView(track);
        body.addView(mine);
        body.addView(logout);

        complaint.setOnClickListener(
                v -> complaint());

        track.setOnClickListener(
                v -> track());

        mine.setOnClickListener(
                v -> mine());

        logout.setOnClickListener(v -> {
            auth.signOut();
            login();
        });
    }

    void complaint() {

        screen("शिकायत दर्ज करें");

        name = new EditText(this);
        name.setHint("नाम");
        body.addView(name);

        category = new Spinner(this);

        String[] categories = {
                "श्रेणी चुनें",
                "पानी",
                "सड़क",
                "बिजली / स्ट्रीट लाइट",
                "सफाई",
                "नाली / जल निकासी",
                "पंचायत संबंधी",
                "कृषि",
                "सरकारी योजना",
                "अन्य"
        };

        category.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        categories));

        body.addView(category);

        details = new EditText(this);
        details.setHint(
                "समस्या का विस्तृत विवरण");
        details.setMinLines(5);
        body.addView(details);

        location = new EditText(this);
        location.setHint(
                "समस्या का स्थान / वार्ड");
        body.addView(location);

        Button submit =
                button("✈️ शिकायत सबमिट करें");

        body.addView(submit);

        status = text("", 16);
        body.addView(status);

        submit.setOnClickListener(v -> {

            String n =
                    name.getText()
                        .toString()
                        .trim();

            String d =
                    details.getText()
                            .toString()
                            .trim();

            String l =
                    location.getText()
                            .toString()
                            .trim();

            String c =
                    category.getSelectedItem()
                            .toString();

            if (n.isEmpty() ||
                d.isEmpty() ||
                l.isEmpty() ||
                c.startsWith("श्रेणी")) {

                status.setText(
                        "सभी जरूरी जानकारी भरें");

                return;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put("name", n);
            data.put("category", c);
            data.put("details", d);
            data.put("location", l);
            data.put("status", "प्राप्त");
            data.put("userId", auth.getUid());
            data.put(
                    "phone",
                    auth.getCurrentUser()
                        .getPhoneNumber());
            data.put(
                    "createdAt",
                    FieldValue.serverTimestamp());

            db.collection("complaints")
              .add(data)
              .addOnSuccessListener(document -> {

                  status.setText(
                          "शिकायत दर्ज हो गई।\n\n" +
                          "Complaint ID:\n" +
                          document.getId());
              })
              .addOnFailureListener(e -> {

                  status.setText(
                          "त्रुटि:\n" +
                          e.getMessage());
              });
        });
    }

    void track() {

        screen("शिकायत की स्थिति");

        EditText id =
                new EditText(this);

        id.setHint("Complaint ID");
        body.addView(id);

        Button search =
                button("स्थिति देखें");

        body.addView(search);

        status = text("", 16);
        body.addView(status);

        search.setOnClickListener(v -> {

            String complaintId =
                    id.getText()
                      .toString()
                      .trim();

            if (complaintId.isEmpty()) {
                status.setText(
                        "Complaint ID डालें");
                return;
            }

            db.collection("complaints")
              .document(complaintId)
              .get()
              .addOnSuccessListener(document -> {

                  if (document.exists()) {

                      status.setText(
                              "स्थिति: " +
                              document.getString(
                                      "status") +
                              "\n\nश्रेणी: " +
                              document.getString(
                                      "category") +
                              "\n\nसमस्या: " +
                              document.getString(
                                      "details"));

                  } else {

                      status.setText(
                              "शिकायत नहीं मिली");
                  }
              })
              .addOnFailureListener(e -> {

                  status.setText(
                          "डेटा प्राप्त नहीं हुआ:\n" +
                          e.getMessage());
              });
        });
    }

    void mine() {

        screen("मेरी शिकायतें");

        status =
                text("लोड हो रहा है...", 16);

        body.addView(status);

        db.collection("complaints")
          .whereEqualTo(
                  "userId",
                  auth.getUid())
          .get()
          .addOnSuccessListener(result -> {

              body.removeView(status);

              if (result.isEmpty()) {

                  body.addView(
                          text(
                                  "अभी कोई शिकायत नहीं है।",
                                  18));

                  return;
              }

              for (DocumentSnapshot d :
                      result) {

                  String info =
                          "Complaint ID:\n" +
                          d.getId() +
                          "\n\nश्रेणी: " +
                          d.getString(
                                  "category") +
                          "\nस्थिति: " +
                          d.getString(
                                  "status");

                  body.addView(
                          text(info, 17));
              }
          })
          .addOnFailureListener(e -> {

              status.setText(
                      "डेटा लोड नहीं हुआ:\n" +
                      e.getMessage());
                        });
    }
}
