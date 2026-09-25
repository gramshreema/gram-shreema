package com.shreema.gramshikayat;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.*;

public class MainActivity extends Activity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    LinearLayout box, body;
    EditText phone, otp, name, details, location;
    Spinner category;
    TextView title, status;

    String verificationId;
    int green = Color.rgb(8,127,67);
    Uri cameraUri;
    String pendingComplaintId;
    String pendingClosureDetails;

    static final int REQ_CAMERA = 501;

    public void onCreate(Bundle b) {
        super.onCreate(b);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (auth.getCurrentUser() != null) {
            home();
        } else {
            login();
        }
    }

    TextView t(String s, int size) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setPadding(10,12,10,12);
        return v;
    }

    Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        return b;
    }

    void base(String heading) {
        box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(24,18,24,24);

        title = t("🌿 ग्राम श्रीमा\n" + heading,25);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(green);
        box.addView(title);

        ScrollView sc = new ScrollView(this);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0,12,0,12);

        sc.addView(body);

        box.addView(sc,
                new LinearLayout.LayoutParams(-1,0,1));

        setContentView(box);
    }

    void login() {
        base("मोबाइल OTP लॉगिन");

        phone = new EditText(this);
        phone.setHint("10 अंकों का मोबाइल नंबर");
        phone.setInputType(3);
        body.addView(phone);

        Button send = btn("OTP भेजें");
        body.addView(send);

        status = t("",15);
        body.addView(status);

        otp = new EditText(this);
        otp.setHint("OTP डालें");
        otp.setInputType(2);
        body.addView(otp);

        Button verify = btn("OTP सत्यापित करें");
        body.addView(verify);

        send.setOnClickListener(v -> {

            String p = phone.getText().toString().trim();

            if (p.length() != 10) {
                status.setText("सही मोबाइल नंबर डालें");
                return;
            }

            PhoneAuthOptions o =
                    PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("+91" + p)
                    .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            public void onVerificationCompleted(
                                    PhoneAuthCredential c) {

                                auth.signInWithCredential(c)
                                    .addOnCompleteListener(x -> {
                                        if (x.isSuccessful()) {
                                            home();
                                        }
                                    });
                            }

                            public void onVerificationFailed(
                                    FirebaseException e) {

                                status.setText(
                                    "OTP नहीं भेजा गया: " +
                                    e.getMessage());
                            }

                            public void onCodeSent(
                                    String id,
                                    PhoneAuthProvider.ForceResendingToken r) {

                                verificationId = id;
                                status.setText(
                                    "OTP भेज दिया गया है।");
                            }
                        })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(o);
        });

        verify.setOnClickListener(v -> {

            if (verificationId == null) {
                status.setText("पहले OTP भेजें");
                return;
            }

            PhoneAuthCredential c =
                    PhoneAuthProvider.getCredential(
                        verificationId,
                        otp.getText().toString().trim());

            auth.signInWithCredential(c)
                .addOnCompleteListener(x -> {

                    if (x.isSuccessful()) {
                        home();
                    } else {
                        status.setText("OTP गलत है");
                    }
                });
        });
    }

    void home() {
        base("जन शिकायत एवं समाधान");

        body.addView(
            t("आपकी समस्या, हमारी जिम्मेदारी",20));

        Button a = btn("📝 शिकायत दर्ज करें");
        Button b = btn("🔎 शिकायत की स्थिति");
        Button c = btn("📋 मेरी शिकायतें");
        Button d = btn("👨‍💼 Admin Panel");
        Button out = btn("लॉगआउट");

        body.addView(a);
        body.addView(b);
        body.addView(c);
        body.addView(d);
        body.addView(out);

        a.setOnClickListener(v -> complaint());
        b.setOnClickListener(v -> track());
        c.setOnClickListener(v -> mine());
        d.setOnClickListener(v -> admin());

        out.setOnClickListener(v -> {
            auth.signOut();
            login();
        });
    }

    void complaint() {
        base("शिकायत दर्ज करें");

        name = new EditText(this);
        name.setHint("नाम");
        body.addView(name);

        category = new Spinner(this);

        String[] cats = {
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
                android.R.layout.simple_spinner_dropdown_item,
                cats));

        body.addView(category);

        details = new EditText(this);
        details.setHint("समस्या का विस्तृत विवरण");
        details.setMinLines(5);
        body.addView(details);

        location = new EditText(this);
        location.setHint("समस्या का स्थान / वार्ड");
        body.addView(location);

        Button submit = btn("✈️ शिकायत सबमिट करें");
        body.addView(submit);

        status = t("",15);
        body.addView(status);

        submit.setOnClickListener(v -> {

            String n = name.getText().toString().trim();
            String d = details.getText().toString().trim();
            String l = location.getText().toString().trim();
            String cat = category.getSelectedItem().toString();

            if (n.isEmpty() ||
                d.isEmpty() ||
                l.isEmpty() ||
                cat.startsWith("श्रेणी")) {

                status.setText("सभी जरूरी जानकारी भरें");
                return;
            }

            Map<String,Object> m = new HashMap<>();

            m.put("name",n);
            m.put("category",cat);
            m.put("details",d);
            m.put("location",l);
            m.put("status","प्राप्त");
            m.put("userId",auth.getUid());
            m.put("phone",
                  auth.getCurrentUser().getPhoneNumber());
            m.put("createdAt",
                  FieldValue.serverTimestamp());

            db.collection("complaints")
              .add(m)
              .addOnSuccessListener(r ->
                  status.setText(
                    "शिकायत दर्ज हो गई। नंबर: " +
                    r.getId()))
              .addOnFailureListener(e ->
                  status.setText(
                    "त्रुटि: " + e.getMessage()));
        });
    }

    void track() {
        base("शिकायत की स्थिति");

        EditText id = new EditText(this);
        id.setHint("Complaint ID");
        body.addView(id);

        Button go = btn("स्थिति देखें");
        body.addView(go);

        status = t("",15);
        body.addView(status);

        go.setOnClickListener(v -> {

            String s = id.getText().toString().trim();

            if (s.isEmpty()) return;

            db.collection("complaints")
              .document(s)
              .get()
              .addOnSuccessListener(x -> {

                  if (x.exists()) {

                      String msg =
                          "स्थिति: " +
                          x.getString("status") +
                          "\nश्रेणी: " +
                          x.getString("category") +
                          "\n" +
                          x.getString("details");

                      if (x.getString("closureDetails") != null) {
                          msg +=
                            "\n\nसमाधान विवरण: " +
                            x.getString("closureDetails");
                      }

                      status.setText(msg);

                  } else {
                      status.setText("शिकायत नहीं मिली");
                  }
              });
        });
    }

    void mine() {
        base("मेरी शिकायतें");

        status = t("लोड हो रहा है...",15);
        body.addView(status);

        db.collection("complaints")
          .whereEqualTo("userId",auth.getUid())
          .get()
          .addOnSuccessListener(q -> {

              body.removeView(status);

              for (DocumentSnapshot x : q) {

                  String s =
                      x.getId() +
                      "\n" +
                      x.getString("category") +
                      "\nस्थिति: " +
                      x.getString("status");

                  if (x.getString("closureDetails") != null) {
                      s +=
                        "\nसमाधान: " +
                        x.getString("closureDetails");
                  }

                  TextView v = t(s,17);
                  v.setPadding(12,18,12,18);

                  body.addView(v);
              }

          })
          .addOnFailureListener(e ->
              status.setText("डेटा लोड नहीं हुआ"));
    }

    void admin() {
        base("Admin Panel");

        status = t(
            "Admin access केवल Firestore में admins/{uid} वाले खाते को मिलेगा.",
            15);

        body.addView(status);

        db.collection("admins")
          .document(auth.getUid())
          .get()
          .addOnSuccessListener(x -> {

              if (!x.exists()) {
                  status.setText("आप Admin नहीं हैं।");
                  return;
              }

              status.setText("Admin verified");
              loadAdmin();
          });
    }

    void loadAdmin() {

        db.collection("complaints")
          .get()
          .addOnSuccessListener(q -> {

              for (DocumentSnapshot x : q) {

                  LinearLayout row =
                      new LinearLayout(this);

                  row.setOrientation(
                      LinearLayout.VERTICAL);

                  row.setPadding(8,12,8,12);

                  row.addView(
                      t(
                        x.getId() +
                        " • " +
                        x.getString("category") +
                        "\n" +
                        x.getString("details") +
                        "\nशिकायतकर्ता: " +
                        x.getString("name"),
                        16));

                  String current =
                      x.getString("status");

                  Spinner sp =
                      new Spinner(this);

                  String[] ss = {
                      "प्राप्त",
                      "कार्रवाई में",
                      "समाधान"
                  };

                  sp.setAdapter(
                      new ArrayAdapter<String>(
                          this,
                          android.R.layout.simple_spinner_dropdown_item,
                          ss));

                  if (current != null) {
                      for (int i=0;i<ss.length;i++) {
                          if (ss[i].equals(current)) {
                              sp.setSelection(i);
                              break;
                          }
                      }
                  }

                  row.addView(sp);

                  Button save =
                      btn("स्थिति अपडेट करें");

                  row.addView(save);

                  Button close =
                      btn("📷 शिकायतकर्ता के साथ मौके की फोटो लेकर क्लोज करें");

                  row.addView(close);

                  save.setOnClickListener(v ->
                      db.collection("complaints")
                        .document(x.getId())
                        .update(
                            "status",
                            sp.getSelectedItem().toString())
                        .addOnSuccessListener(z ->
                            Toast.makeText(
                                this,
                                "स्थिति अपडेट हो गई",
                                Toast.LENGTH_SHORT).show()));

                  close.setOnClickListener(v ->
                      showCloseDialog(x.getId()));

                  body.addView(row);
              }

          })
          .addOnFailureListener(e ->
              status.setText(
                  "शिकायतें लोड नहीं हुईं: " +
                  e.getMessage()));
    }

    void showCloseDialog(String complaintId) {

        final EditText detail =
            new EditText(this);

        detail.setHint(
            "समाधान का पूरा विवरण लिखें");

        detail.setMinLines(4);
        detail.setGravity(Gravity.TOP);

        LinearLayout wrap =
            new LinearLayout(this);

        wrap.setOrientation(
            LinearLayout.VERTICAL);

        wrap.setPadding(20,5,20,5);

        wrap.addView(
            t(
              "अधिकारी शिकायतकर्ता के पास जाकर समाधान की फोटो लें",
              16));

        wrap.addView(detail);

        new AlertDialog.Builder(this)
            .setTitle("शिकायत क्लोज करें")
            .setView(wrap)
            .setPositiveButton(
                "📷 फोटो लें",
                (d,w) -> {

                    String text =
                        detail.getText()
                              .toString()
                              .trim();

                    if (text.isEmpty()) {

                        Toast.makeText(
                            this,
                            "पहले समाधान का विवरण लिखें",
                            Toast.LENGTH_LONG).show();

                        return;
                    }

                    pendingComplaintId =
                        complaintId;

                    pendingClosureDetails =
                        text;

                    openCamera();
                })
            .setNegativeButton(
                "रद्द करें",
                null)
            .show();
    }

    void openCamera() {

        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                REQ_CAMERA);

            return;
        }

        launchCamera();
    }

    void launchCamera() {

        try {

            File dir =
                new File(
                    getExternalFilesDir(
                        android.os.Environment.DIRECTORY_PICTURES),
                    "closures");

            if (!dir.exists())
                dir.mkdirs();

            File f =
                new File(
                    dir,
                    "closure_" +
                    System.currentTimeMillis() +
                    ".jpg");

            cameraUri =
                FileProvider.getUriForFile(
                    this,
                    getPackageName() +
                    ".fileprovider",
                    f);

            Intent i =
                new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);

            i.putExtra(
                MediaStore.EXTRA_OUTPUT,
                cameraUri);

            i.addFlags(
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(
                i,
                REQ_CAMERA);

        } catch (Exception e) {

            Toast.makeText(
                this,
                "कैमरा शुरू नहीं हुआ: " +
                e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults);

        if (requestCode == REQ_CAMERA &&
            grantResults.length > 0 &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED) {

            launchCamera();

        } else if (requestCode == REQ_CAMERA) {

            Toast.makeText(
                this,
                "कैमरा अनुमति जरूरी है",
                Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data);

                if (requestCode == REQ_CAMERA) {

            if (resultCode == RESULT_OK &&
                cameraUri != null &&
                pendingComplaintId != null) {

                uploadClosurePhoto();

            } else {

                Toast.makeText(
                    this,
                    "फोटो नहीं ली गई",
                    Toast.LENGTH_SHORT
                ).show();
                    }
    }

        protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data);

        if (requestCode == REQ_CAMERA) {

            if (resultCode == RESULT_OK &&
                cameraUri != null &&
                pendingComplaintId != null) {

                Toast.makeText(
                    this,
                    "फोटो ली गई",
                    Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                    this,
                    "फोटो नहीं ली गई",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
    }
