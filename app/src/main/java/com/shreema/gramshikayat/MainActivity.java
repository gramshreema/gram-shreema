package com.shreema.gramshikayat;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.text.InputType;
import android.widget.*;
import android.content.Intent;
import android.provider.MediaStore;
import android.net.Uri;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class MainActivity extends Activity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    LinearLayout body;

    EditText email;
    EditText password;
    EditText name;
    EditText details;
    EditText location;

    Spinner category;
    TextView status;

    String lastComplaintId = "";
    Uri selectedImageUri;
    Uri solutionImageUri;

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

    // =========================
    // LOGIN
    // =========================

    void login() {

        screen("Email + Password लॉगिन");

        email = new EditText(this);
        email.setHint("Email डालें");
        email.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        body.addView(email);

        password = new EditText(this);
        password.setHint("Password डालें");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        body.addView(password);

        Button loginButton =
                button("🔐 लॉगिन करें");
        body.addView(loginButton);

        Button registerButton =
                button("📝 नया अकाउंट बनाएँ");
        body.addView(registerButton);

        Button forgotPasswordButton =
                button("🔑 Password भूल गए?");
        body.addView(forgotPasswordButton);

        Button forgotEmailButton =
                button("📧 Email भूल गए?");
        body.addView(forgotEmailButton);

        status = text("", 16);
        body.addView(status);

        loginButton.setOnClickListener(v -> {

            String e =
                    email.getText()
                            .toString()
                            .trim();

            String p =
                    password.getText()
                            .toString()
                            .trim();

            if (e.isEmpty() || p.isEmpty()) {

                status.setText(
                        "Email और Password डालें");
                return;
            }

            auth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            home();
                        } else {
                            status.setText(
                                    "लॉगिन नहीं हुआ:\n" +
                                    task.getException()
                                            .getMessage());
                        }
                    });
        });

        registerButton.setOnClickListener(
                v -> register());

        forgotPasswordButton.setOnClickListener(
                v -> forgotPassword());

        forgotEmailButton.setOnClickListener(
                v -> forgotEmail());
    }

    // =========================
    // REGISTER
    // =========================

    void register() {

        screen("नया अकाउंट बनाएँ");

        email = new EditText(this);
        email.setHint("Email डालें");
        email.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        body.addView(email);

        password = new EditText(this);
        password.setHint(
                "Password डालें (कम से कम 6 अक्षर)");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        body.addView(password);

        Button create =
                button("✅ अकाउंट बनाएँ");
        body.addView(create);

        Button back =
                button("⬅️ वापस लॉगिन");
        body.addView(back);

        status = text("", 16);
        body.addView(status);

        create.setOnClickListener(v -> {

            String e =
                    email.getText()
                            .toString()
                            .trim();

            String p =
                    password.getText()
                            .toString()
                            .trim();

            if (e.isEmpty() || p.isEmpty()) {

                status.setText(
                        "Email और Password डालें");
                return;
            }

            if (p.length() < 6) {

                status.setText(
                        "Password कम से कम 6 अक्षर का होना चाहिए");
                return;
            }

            auth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            home();
                        } else {
                            status.setText(
                                    "अकाउंट नहीं बना:\n" +
                                    task.getException()
                                            .getMessage());
                        }
                    });
        });

        back.setOnClickListener(
                v -> login());
    }

    // =========================
    // FORGOT PASSWORD
    // =========================

    void forgotPassword() {

        screen("Password Reset");

        EditText resetEmail =
                new EditText(this);

        resetEmail.setHint(
                "अपना Email डालें");

        resetEmail.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        body.addView(resetEmail);

        Button send =
                button("📩 Reset Email भेजें");

        body.addView(send);

        Button back =
                button("⬅️ वापस लॉगिन");

        body.addView(back);

        status = text("", 16);
        body.addView(status);

        send.setOnClickListener(v -> {

            String e =
                    resetEmail.getText()
                            .toString()
                            .trim();

            if (e.isEmpty()) {

                status.setText(
                        "Email डालें");
                return;
            }

            auth.sendPasswordResetEmail(e)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            status.setText(
                                    "✅ Password Reset लिंक आपके Email पर भेज दिया गया है।");

                        } else {

                            status.setText(
                                    "Reset Email नहीं भेजा गया:\n" +
                                    task.getException()
                                            .getMessage());
                        }
                    });
        });

        back.setOnClickListener(
                v -> login());
    }

    // =========================
    // FORGOT EMAIL
    // =========================

    void forgotEmail() {

        screen("Email सहायता");

        body.addView(
                text(
                        "यदि आपको अपना Login Email याद नहीं है,\n\n" +
                        "तो अपने Email खातों में Firebase से आए " +
                        "संदेश खोजें।\n\n" +
                        "सुरक्षा कारणों से ऐप किसी User का Email " +
                        "बिना पहचान सत्यापन के नहीं दिखाएगा।",
                        18));

        Button back =
                button("⬅️ वापस लॉगिन");

        body.addView(back);

        back.setOnClickListener(
                v -> login());
    }

    // =========================
    // HOME
    // =========================

    void home() {

    screen("ग्राम श्रीमा शिकायत ऐप");

    body.addView(
            text(
                    "🌾 ग्राम श्रीमा",
                    26));

    body.addView(
            text(
                    "जन शिकायत एवं समाधान",
                    21));

    body.addView(
            text(
                    "आपकी समस्या, हमारी जिम्मेदारी",
                    17));

    body.addView(
            text(
                    "शिकायत दर्ज करें और अपनी शिकायत की स्थिति देखें।",
                    15));

    Button complaintButton =
            button("📝 शिकायत दर्ज करें");

    Button trackButton =
            button("🔎 शिकायत की स्थिति");

    Button mineButton =
            button("📋 मेरी शिकायतें");

    Button adminButton =
        button("🛠️ Admin Panel");

Button superAdminButton =
        button("👑 Super Admin Panel");
        Button aboutButton =
        button("ℹ️ About");

Button logoutButton =
        button("🚪 लॉगआउट");

    body.addView(complaintButton);
    body.addView(trackButton);
    body.addView(mineButton);
    body.addView(adminButton);
body.addView(superAdminButton);
        body.addView(aboutButton);
body.addView(logoutButton);

        superAdminButton.setOnClickListener(
        v -> superAdminPanel());
        aboutButton.setOnClickListener(
        v -> about());
    complaintButton.setOnClickListener(
            v -> complaint());

    trackButton.setOnClickListener(
            v -> track());

    mineButton.setOnClickListener(
            v -> mine());

    adminButton.setOnClickListener(
            v -> adminPanel());

    logoutButton.setOnClickListener(v -> {

        auth.signOut();
        login();
    });
            }
        // =========================
    // ABOUT
    // =========================

    void about() {

        screen("About");

        ImageView profile = new ImageView(this);

        profile.setImageResource(
                android.R.drawable.ic_menu_myplaces);

        profile.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams imageParams =
                new LinearLayout.LayoutParams(
                        220, 220);

        imageParams.gravity =
                android.view.Gravity.CENTER;

        body.addView(profile, imageParams);

        TextView appName =
                text(
                        "🌿 ग्राम श्रीमा शिकायत ऐप",
                        24);

        appName.setGravity(
                android.view.Gravity.CENTER);

        appName.setTextColor(
                Color.rgb(8, 127, 67));

        body.addView(appName);

        TextView village =
                text(
                        "ग्राम श्रीमा",
                        20);

        village.setGravity(
                android.view.Gravity.CENTER);

        body.addView(village);

        body.addView(
                text(
                        "जन शिकायत एवं समाधान",
                        19));

        body.addView(
                text(
                        "इस ऐप के माध्यम से ग्रामवासियों की " +
                        "समस्याओं और शिकायतों को दर्ज कर " +
                        "उनकी स्थिति देखने की सुविधा प्रदान की जाती है।",
                        16));

        body.addView(
                text(
                        "आपकी समस्या, हमारी जिम्मेदारी",
                        18));

        Button backButton =
                button("⬅️ वापस");

        body.addView(backButton);

        backButton.setOnClickListener(
                v -> home());
    }

    // =========================
    // COMPLAINT
    // =========================

   void complaint() {

    screen("शिकायत दर्ज करें");

    Button homeButton =
            button("🏠 होम पर जाएँ");

    body.addView(homeButton);

    homeButton.setOnClickListener(
            v -> home());

    name = new EditText(this);
    name.setHint("नाम");
    body.addView(name);

    EditText phone =
            new EditText(this);

    phone.setHint("📱 मोबाइल नंबर");
    phone.setInputType(
            InputType.TYPE_CLASS_PHONE);

    body.addView(phone);

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
       Button photoButton =
        button("📷 शिकायत की फोटो चुनें");

body.addView(photoButton);

photoButton.setOnClickListener(v -> {

    Intent intent =
            new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

    startActivityForResult(
            intent,
            1001);
});

    Button submit =
            button("✈️ शिकायत सबमिट करें");

    body.addView(submit);

    status = text("", 16);
    body.addView(status);

    submit.setOnClickListener(v -> {
        if (selectedImageUri == null) {

    status.setText(
            "📷 कृपया शिकायत की फोटो चुनें");

    return;
        }

        String n =
                name.getText()
                        .toString()
                        .trim();

        String p =
                phone.getText()
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
                p.isEmpty() ||
                d.isEmpty() ||
                l.isEmpty() ||
                c.startsWith("श्रेणी")) {

            status.setText(
                    "सभी जरूरी जानकारी भरें");
            return;
        }

        if (p.length() < 10) {

            status.setText(
                    "सही 10 अंकों का मोबाइल नंबर डालें");
            return;
        }

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            status.setText(
                    "पहले लॉगिन करें");
            return;
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put("name", n);
        data.put("phone", p);
        data.put("category", c);
        data.put("details", d);
        data.put("location", l);
        data.put("status", "प्राप्त");
        data.put("userId", user.getUid());
        data.put("email", user.getEmail());

        data.put(
                "createdAt",
                FieldValue.serverTimestamp());

    new Thread(() -> {

    try {

        String cloudName = "yfva6qyg";
        String uploadPreset = "shreema_complaint";

        String boundary =
                "----ShreemaBoundary" +
                System.currentTimeMillis();

        URL url = new URL(
                "https://api.cloudinary.com/v1_1/" +
                cloudName +
                "/image/upload");

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=" +
                boundary);

        OutputStream output =
                connection.getOutputStream();

        String lineEnd = "\r\n";

        output.write((
                "--" + boundary + lineEnd +
                "Content-Disposition: form-data; name=\"upload_preset\"" +
                lineEnd + lineEnd +
                uploadPreset + lineEnd
        ).getBytes("UTF-8"));

        output.write((
                "--" + boundary + lineEnd +
                "Content-Disposition: form-data; name=\"file\"; filename=\"complaint.jpg\"" +
                lineEnd +
                "Content-Type: image/jpeg" +
                lineEnd + lineEnd
        ).getBytes("UTF-8"));

        InputStream input =
                getContentResolver()
                        .openInputStream(
                                selectedImageUri);

        byte[] buffer =
                new byte[4096];

        int length;

        while ((length =
                input.read(buffer)) != -1) {

            output.write(
                    buffer,
                    0,
                    length);
        }

        input.close();

        output.write(
                (lineEnd +
                 "--" +
                 boundary +
                 "--" +
                 lineEnd)
                        .getBytes("UTF-8"));

        output.flush();
        output.close();

        int responseCode =
                connection.getResponseCode();

        InputStream responseStream;

        if (responseCode >= 200 &&
                responseCode < 300) {

            responseStream =
                    connection.getInputStream();

        } else {

            responseStream =
                    connection.getErrorStream();
        }

        java.util.Scanner scanner =
                new java.util.Scanner(
                        responseStream)
                        .useDelimiter("\\A");

        String response =
                scanner.hasNext()
                        ? scanner.next()
                        : "";

        scanner.close();

        if (responseCode >= 200 &&
                responseCode < 300) {

            JSONObject json =
                    new JSONObject(response);

            String photoUrl =
                    json.getString("secure_url");

            data.put(
                    "photoUrl",
                    photoUrl);

            runOnUiThread(() -> {

                db.collection("complaints")
                        .add(data)
                        .addOnSuccessListener(document -> {

                            lastComplaintId =
                                    document.getId();

                            status.setText(
                                    "✅ शिकायत दर्ज हो गई।\n\n" +
                                    "Complaint ID:\n" +
                                    lastComplaintId);

                            Button viewStatus =
                                    button(
                                            "🔎 इसी शिकायत की स्थिति देखें");

                            Button goHome =
                                    button(
                                            "🏠 होम पर जाएँ");

                            body.addView(viewStatus);
                            body.addView(goHome);

                            viewStatus.setOnClickListener(
                                    click ->
                                            trackById(
                                                    lastComplaintId));

                            goHome.setOnClickListener(
                                    click ->
                                            home());
                        })
                        .addOnFailureListener(e -> {

                            status.setText(
                                    "शिकायत सेव नहीं हुई:\n" +
                                    e.getMessage());
                        });
            });

        } else {

            runOnUiThread(() -> {

                status.setText(
                        "📷 फोटो अपलोड नहीं हुई:\n" +
                        response);
            });
        }

        connection.disconnect();

    } catch (Exception e) {

        runOnUiThread(() -> {

            status.setText(
                    "📷 फोटो अपलोड में समस्या:\n" +
                    e.getMessage());
        });
    }

}).start();

    });

}

// =========================
// TRACK
// =========================

void track() {

        screen("शिकायत की स्थिति");

        EditText id =
                new EditText(this);

        id.setHint("Complaint ID");
        id.setInputType(InputType.TYPE_CLASS_TEXT);
        body.addView(id);

        Button search =
                button("🔎 स्थिति देखें");

        body.addView(search);

        Button homeButton =
                button("🏠 होम पर जाएँ");

        body.addView(homeButton);

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

            trackById(complaintId);
        });

        homeButton.setOnClickListener(
                v -> home());
    }

// =========================
// TRACK BY ID
// =========================

void trackById(String complaintId) {

    screen("शिकायत की स्थिति");

    status = text(
            "शिकायत खोजी जा रही है...",
            16);

    body.addView(status);

    Button homeButton =
            button("🏠 होम पर जाएँ");

    body.addView(homeButton);

    homeButton.setOnClickListener(
            click -> home());

    FirebaseUser user =
            auth.getCurrentUser();

    if (user == null) {
        status.setText("पहले Login करें");
        return;
    }

    db.collection("complaints")
            .get()
            .addOnSuccessListener(result -> {

                DocumentSnapshot found = null;

                String searchId =
                        complaintId.trim();

                for (DocumentSnapshot d :
                        result.getDocuments()) {

                    String documentId =
                            d.getId().trim();

                    if (documentId.equalsIgnoreCase(searchId)) {
                        found = d;
                        break;
                    }
                }

                if (found != null) {

                    showComplaint(found);

                } else {

                    status.setText(
                            "शिकायत नहीं मिली\n\n" +
                            "कृपया Complaint ID सही डालें:\n\n" +
                            searchId);
                }

            })
            .addOnFailureListener(e -> {

                status.setText(
                        "डेटा प्राप्त नहीं हुआ:\n" +
                        e.getMessage());
            });
                        }

    // =========================
    // SHOW COMPLAINT
    // =========================

    void showComplaint(DocumentSnapshot document) {

    String actionDetails =
            document.getString("actionDetails");

    if (actionDetails == null ||
            actionDetails.trim().isEmpty()) {

        actionDetails =
                "अभी कोई कार्यवाही विवरण नहीं दिया गया।";
    }

    // शिकायत की तारीख और समय
    Object createdAt = document.get("createdAt");

    String dateTime = "उपलब्ध नहीं";

    if (createdAt instanceof com.google.firebase.Timestamp) {

        java.util.Date date =
                ((com.google.firebase.Timestamp) createdAt)
                        .toDate();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(
                        "dd-MM-yyyy hh:mm a",
                        java.util.Locale.getDefault());

        dateTime = sdf.format(date);
    }

    status.setText(
            "Complaint ID:\n" +
            document.getId() +

            "\n\nदर्ज करने की तारीख और समय:\n" +
            dateTime +

            "\n\nस्थिति: " +
            document.getString("status") +

            "\n\nश्रेणी: " +
            document.getString("category") +

            "\n\nसमस्या: " +
            document.getString("details") +

            "\n\nस्थान: " +
            document.getString("location") +

            "\n\nकार्यवाही / समाधान:\n" +
            actionDetails);
        // =========================
// समाधान की फोटो
// =========================

String solutionPhotoUrl =
        document.getString("solutionPhotoUrl");

if (solutionPhotoUrl != null &&
        !solutionPhotoUrl.trim().isEmpty()) {

    ImageView solutionPhoto =
            new ImageView(this);

    solutionPhoto.setLayoutParams(
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    500));

    solutionPhoto.setScaleType(
            ImageView.ScaleType.CENTER_CROP);

    body.addView(solutionPhoto);

    com.bumptech.glide.Glide
            .with(this)
            .load(solutionPhotoUrl)
            .into(solutionPhoto);
}
    }

    // =========================
    // MY COMPLAINTS
    // =========================

   void mine() {

    screen("मेरी शिकायतें");

    status = text(
            "शिकायतें लोड हो रही हैं...",
            16);

    body.addView(status);

    Button homeButton =
            button("🏠 होम पर जाएँ");

    body.addView(homeButton);

    homeButton.setOnClickListener(
            v -> home());

    FirebaseUser user =
            auth.getCurrentUser();

    if (user == null) {

        status.setText(
                "पहले Login करें");
        return;
    }

    db.collection("complaints")
            .whereEqualTo(
                    "userId",
                    user.getUid())
            .get()
            .addOnSuccessListener(result -> {

                status.setText("");

                if (result.isEmpty()) {

                    status.setText(
                            "आपकी कोई शिकायत नहीं मिली");
                    return;
                }

                java.util.List<DocumentSnapshot> complaints =
        new java.util.ArrayList<>(
                result.getDocuments());

java.util.Collections.sort(
        complaints,
        (a, b) -> {

            com.google.firebase.Timestamp ta =
                    a.getTimestamp("createdAt");

            com.google.firebase.Timestamp tb =
                    b.getTimestamp("createdAt");

            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;

            return tb.compareTo(ta);
        });

for (DocumentSnapshot d : complaints) {
                    

                    // तारीख और समय
                    Object createdAt =
                            d.get("createdAt");

                    String dateTime =
                            "उपलब्ध नहीं";

                    if (createdAt instanceof
                            com.google.firebase.Timestamp) {

                        java.util.Date date =
                                ((com.google.firebase.Timestamp)
                                        createdAt)
                                        .toDate();

                        java.text.SimpleDateFormat sdf =
                                new java.text.SimpleDateFormat(
                                        "dd-MM-yyyy hh:mm a",
                                        java.util.Locale.getDefault());

                        dateTime =
                                sdf.format(date);
                    }

                    // कार्यवाही / समाधान
                    String actionDetails =
                            d.getString(
                                    "actionDetails");

                    if (actionDetails == null ||
                            actionDetails.trim().isEmpty()) {

                        actionDetails =
                                "अभी कोई कार्यवाही विवरण नहीं दिया गया।";
                    }

                    String info =
                            "Complaint ID:\n" +
                            d.getId() +

                            "\n\nदर्ज करने की तारीख और समय:\n" +
                            dateTime +

                            "\n\nश्रेणी: " +
                            d.getString("category") +

                            "\n\nस्थिति: " +
                            d.getString("status") +

                            "\n\nसमस्या:\n" +
                            d.getString("details") +

                            "\n\nकार्यवाही / समाधान:\n" +
                            actionDetails +

                            "\n\n";

                    body.addView(
                            text(info, 17));

                    Button view =
                            button(
                                    "🔎 पूरी स्थिति देखें");

                    body.addView(view);

                    String complaintId =
                            d.getId();

                    view.setOnClickListener(
                            v ->
                                    trackById(
                                            complaintId));
                }
            })
            .addOnFailureListener(e -> {

                status.setText(
                        "डेटा लोड नहीं हुआ:\n" +
                        e.getMessage());
            });
                        }  

   void adminPanel() {

    screen("🛠️ Admin Panel");

    status = text(
            "शिकायतें लोड हो रही हैं...",
            16);

    body.addView(status);

    // शिकायतों की गिनती दिखाने वाला TextView
    TextView countStatus = text(
            "📊 शिकायतों की गिनती लोड हो रही है...",
            17);

    body.addView(countStatus);

    Button homeButton =
            button("🏠 होम पर जाएँ");

    body.addView(homeButton);

    homeButton.setOnClickListener(
            v -> home());

    FirebaseUser user =
            auth.getCurrentUser();

    if (user == null) {

        status.setText(
                "पहले Login करें");
        return;
    }

    db.collection("admins")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(adminDoc -> {

                if (!adminDoc.exists()) {

                    status.setText(
                            "❌ आपको Admin की अनुमति नहीं है");

                    countStatus.setText("");

                    return;
                }

                db.collection("complaints")
                        .whereNotEqualTo(
                                "status",
                                "निस्तारित")
                        .get()
                        .addOnSuccessListener(result -> {

                            status.setText("");

                            int activeCount = 0;
                            int receivedCount = 0;
                            int inProgressCount = 0;

                            java.util.List<DocumentSnapshot> complaints =
                                    new java.util.ArrayList<>(
                                            result.getDocuments());

                            java.util.Collections.sort(
                                    complaints,
                                    (a, b) -> {

                                        com.google.firebase.Timestamp ta =
                                                a.getTimestamp("createdAt");

                                        com.google.firebase.Timestamp tb =
                                                b.getTimestamp("createdAt");

                                        if (ta == null && tb == null)
                                            return 0;

                                        if (ta == null)
                                            return 1;

                                        if (tb == null)
                                            return -1;

                                        return tb.compareTo(ta);
                                    });

                            for (DocumentSnapshot d : complaints) {

                                String currentStatus =
                                        d.getString("status");

                                if ("निस्तारित".equals(
                                        currentStatus)) {
                                    continue;
                                }

                                activeCount++;

                                if ("प्राप्त".equals(
                                        currentStatus)) {

                                    receivedCount++;

                                } else if ("कार्यवाही जारी".equals(
                                        currentStatus)) {

                                    inProgressCount++;
                                }

                                Object createdAt =
                                        d.get("createdAt");

                                String dateTime =
                                        "उपलब्ध नहीं";

                                if (createdAt instanceof
                                        com.google.firebase.Timestamp) {

                                    java.util.Date date =
                                            ((com.google.firebase.Timestamp)
                                                    createdAt)
                                                    .toDate();

                                    java.text.SimpleDateFormat sdf =
                                            new java.text.SimpleDateFormat(
                                                    "dd-MM-yyyy hh:mm a",
                                                    java.util.Locale
                                                            .getDefault());

                                    dateTime =
                                            sdf.format(date);
                                }

                                String info =
                                        "Complaint ID:\n" +
                                        d.getId() +

                                        "\n\nदर्ज करने की तारीख और समय:\n" +
                                        dateTime +

                                        "\n\nनाम: " +
                                        d.getString("name") +

                                        "\nमोबाइल: " +
                                        d.getString("phone") +

                                        "\nश्रेणी: " +
                                        d.getString("category") +

                                        "\nस्थिति: " +
                                        currentStatus +

                                        "\n\nसमस्या:\n" +
                                        d.getString("details") +

                                        "\n\nस्थान: " +
                                        d.getString("location") +

                                        "\n\n";

                                body.addView(
                                        text(info, 16));

                                Button callButton =
                                        button("📞 कॉल करें");
                                body.addView(
        text(info, 16));

String photoUrl =
        d.getString("photoUrl");
                                if (photoUrl != null &&
        !photoUrl.trim().isEmpty()) {

    ImageView photoPreview =
            new ImageView(this);

    photoPreview.setLayoutParams(
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    500));

    photoPreview.setScaleType(
            ImageView.ScaleType.CENTER_CROP);

    body.addView(photoPreview);

    com.bumptech.glide.Glide
            .with(this)
            .load(photoUrl)
            .into(photoPreview);
                                }

if (photoUrl != null &&
        !photoUrl.trim().isEmpty()) {

    Button photoButton =
            button("📷 शिकायत की फोटो देखें");

    body.addView(photoButton);

    photoButton.setOnClickListener(
            v -> {

                android.content.Intent intent =
                        new android.content.Intent(
                                android.content.Intent.ACTION_VIEW);

                intent.setData(
                        android.net.Uri.parse(
                                photoUrl));

                startActivity(intent);
            });
}

                                body.addView(callButton);

                                String phoneNumber =
                                        d.getString("phone");

                                callButton.setOnClickListener(
                                        v -> {

                                            if (phoneNumber != null &&
                                                    !phoneNumber
                                                            .trim()
                                                            .isEmpty()) {

                                                android.content.Intent intent =
                                                        new android.content.Intent(
                                                                android.content.Intent
                                                                        .ACTION_DIAL);

                                                intent.setData(
                                                        android.net.Uri.parse(
                                                                "tel:" +
                                                                        phoneNumber));

                                                startActivity(intent);

                                            } else {

                                                Toast.makeText(
                                                        this,
                                                        "मोबाइल नंबर उपलब्ध नहीं है",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });

                                Button statusButton =
                                        button(
                                                "✏️ स्थिति बदलें");

                                body.addView(
                                        statusButton);

                                String complaintId =
                                        d.getId();

                                statusButton.setOnClickListener(
                                        v ->
                                                changeStatus(
                                                        complaintId));
                            }

                            // शिकायतों की गिनती
                            countStatus.setText(
                                    "📊 कुल सक्रिय शिकायतें: " +
                                            activeCount +

                                            "\n📥 प्राप्त: " +
                                            receivedCount +

                                            "\n🔄 कार्यवाही जारी: " +
                                            inProgressCount);

                            if (activeCount == 0) {

                                status.setText(
                                        "✅ सभी शिकायतों का निस्तारण हो चुका है।");
                            }

                        })
                        .addOnFailureListener(e -> {

                            status.setText(
                                    "शिकायतें लोड नहीं हुईं:\n" +
                                            e.getMessage());

                            countStatus.setText("");
                        });
            })
            .addOnFailureListener(e -> {

                status.setText(
                        "Admin जाँच में समस्या:\n" +
                                e.getMessage());

                countStatus.setText("");
            });
   } 
  void changeStatus(String complaintId) {

    screen("शिकायत की स्थिति बदलें");

    status = text(
            "स्थिति लोड हो रही है...",
            16);

    body.addView(status);

    Spinner statusSpinner =
            new Spinner(this);

    String[] statuses = {
            "प्राप्त",
            "कार्यवाही जारी",
            "निस्तारित"
    };

    statusSpinner.setAdapter(
            new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    statuses));

    body.addView(statusSpinner);

    EditText actionDetails =
            new EditText(this);

    actionDetails.setHint(
            "कार्यवाही / समाधान विवरण");

    actionDetails.setMinLines(4);

    body.addView(actionDetails);
      Button solutionPhotoButton =
        button("📷 समाधान की फोटो चुनें");

body.addView(solutionPhotoButton);

solutionPhotoButton.setOnClickListener(v -> {

    Intent intent =
            new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

    startActivityForResult(
            intent,
            2001);
});

    Button save =
            button("💾 स्थिति सेव करें");

    body.addView(save);

    Button homeButton =
            button("🏠 होम पर जाएँ");

    body.addView(homeButton);

    homeButton.setOnClickListener(
            v -> home());

    save.setOnClickListener(v -> {

    String newStatus =
            statusSpinner
                    .getSelectedItem()
                    .toString();

    String details =
            actionDetails.getText()
                    .toString()
                    .trim();

    if (newStatus.equals("निस्तारित")
            && details.isEmpty()) {

        status.setText(
                "निस्तारित करने से पहले\n" +
                "कार्यवाही / समाधान विवरण लिखें");

        return;
    }

    if (newStatus.equals("निस्तारित")
            && solutionImageUri == null) {

        status.setText(
                "📷 निस्तारित करने से पहले\n" +
                "समाधान की फोटो चुनें");

        return;
    }

    // सामान्य स्थिति: प्राप्त / कार्यवाही जारी
    if (!newStatus.equals("निस्तारित")) {

        db.collection("complaints")
                .document(complaintId)
                .update(
                        "status",
                        newStatus,
                        "actionDetails",
                        details)
                .addOnSuccessListener(aVoid -> {

                    status.setText(
                            "✅ स्थिति अपडेट हो गई\n\n" +
                            "नई स्थिति: " +
                            newStatus +
                            "\n\n" +
                            "कार्यवाही / समाधान:\n" +
                            details);
                })
                .addOnFailureListener(e -> {

                    status.setText(
                            "स्थिति अपडेट नहीं हुई:\n" +
                            e.getMessage());
                });

        return;
    }

    // =========================
    // समाधान की फोटो Cloudinary पर अपलोड
    // =========================

    status.setText(
            "📷 समाधान की फोटो अपलोड हो रही है...");

    new Thread(() -> {

        try {

            String cloudName = "yfva6qyg";
            String uploadPreset =
                    "shreema_complaint";

            String boundary =
                    "----ShreemaSolution" +
                    System.currentTimeMillis();

            URL url =
                    new URL(
                            "https://api.cloudinary.com/v1_1/" +
                            cloudName +
                            "/image/upload");

            HttpURLConnection connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" +
                            boundary);

            OutputStream output =
                    connection.getOutputStream();

            String lineEnd = "\r\n";

            output.write((
                    "--" + boundary + lineEnd +
                    "Content-Disposition: form-data; name=\"upload_preset\"" +
                    lineEnd + lineEnd +
                    uploadPreset + lineEnd
            ).getBytes("UTF-8"));

            output.write((
                    "--" + boundary + lineEnd +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"solution.jpg\"" +
                    lineEnd +
                    "Content-Type: image/jpeg" +
                    lineEnd + lineEnd
            ).getBytes("UTF-8"));

            InputStream input =
                    getContentResolver()
                            .openInputStream(
                                    solutionImageUri);

            byte[] buffer =
                    new byte[4096];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length);
            }

            input.close();

            output.write(
                    (lineEnd +
                            "--" +
                            boundary +
                            "--" +
                            lineEnd)
                            .getBytes("UTF-8"));

            output.flush();
            output.close();

            int responseCode =
                    connection.getResponseCode();

            InputStream responseStream;

            if (responseCode >= 200 &&
                    responseCode < 300) {

                responseStream =
                        connection.getInputStream();

            } else {

                responseStream =
                        connection.getErrorStream();
            }

            java.util.Scanner scanner =
                    new java.util.Scanner(
                            responseStream)
                            .useDelimiter("\\A");

            String response =
                    scanner.hasNext()
                            ? scanner.next()
                            : "";

            scanner.close();

            if (responseCode >= 200 &&
                    responseCode < 300) {

                JSONObject json =
                        new JSONObject(response);

                String solutionPhotoUrl =
                        json.getString(
                                "secure_url");

                runOnUiThread(() -> {
                  db.collection("complaints")
                            .document(complaintId)
                            .update(
                                    "status",
                                    newStatus,
                                    "actionDetails",
                                    details,
                                    "solutionPhotoUrl",
                                    solutionPhotoUrl)
                            .addOnSuccessListener(
                                    aVoid -> {

                                status.setText(
                                        "✅ शिकायत निस्तारित हो गई\n\n" +
                                        "नई स्थिति: निस्तारित\n\n" +
                                        "कार्यवाही / समाधान:\n" +
                                        details);
                            })
                            .addOnFailureListener(
                                    e -> {

                                status.setText(
                                        "स्थिति अपडेट नहीं हुई:\n" +
                                        e.getMessage());
                            });
                });

            } else {

                runOnUiThread(() -> {

                    status.setText(
                            "📷 समाधान की फोटो अपलोड नहीं हुई:\n" +
                            response);
                });
            }

            connection.disconnect();

        } catch (Exception e) {

            runOnUiThread(() -> {

                status.setText(
                        "📷 समाधान की फोटो अपलोड में समस्या:\n" +
                        e.getMessage());
            });
        }
            }).start();
});

}   // changeStatus() बंद

// =========================
// SUPER ADMIN PANEL
// =========================

void superAdminPanel() {

    screen("👑 Super Admin Panel");

    status = text(
            "Super Admin की अनुमति जाँची जा रही है...",
            16);

    body.addView(status);

    Button homeButton =
            button("🏠 होम पर जाएँ");

    body.addView(homeButton);

    homeButton.setOnClickListener(
            v -> home());

    FirebaseUser user =
            auth.getCurrentUser();

    if (user == null) {

        status.setText(
                "❌ पहले Login करें");
        return;
    }

    String uid =
            user.getUid();

    db.collection("admins")
            .document(uid)
            .get()
            .addOnSuccessListener(adminDoc -> {

                try {

                    if (!adminDoc.exists()) {

                        status.setText(
                                "❌ Admin की अनुमति नहीं है");
                        return;
                    }

                    Object roleValue =
        adminDoc.get("role");

String role =
        roleValue == null
                ? ""
                : String.valueOf(roleValue);

Boolean active =
        adminDoc.getBoolean("active");

                    if (role == null) {
                        role = "";
                    }

                    if (!Boolean.TRUE.equals(active)) {

                        status.setText(
                                "❌ आपका Admin Account बंद है");
                        return;
                    }

                    if (!"superadmin".equals(role)) {

                        status.setText(
                                "❌ केवल Super Admin इस पैनल को खोल सकता है");
                        return;
                    }

                    status.setText(
                            "✅ Super Admin की अनुमति है");

                    Button addAdminButton =
                            button("➕ नया Admin जोड़ें");

                    body.addView(addAdminButton);

                    addAdminButton.setOnClickListener(
                            v -> addAdmin());

                    Button manageAdminButton =
                            button("👥 Admin की सूची / प्रबंधन");

                    body.addView(manageAdminButton);

                    manageAdminButton.setOnClickListener(
                            v -> manageAdmins());

                } catch (Exception e) {

                    status.setText(
                            "❌ Super Admin Panel में समस्या:\n\n" +
                            e.getMessage());
                }

            })
            .addOnFailureListener(e -> {

                status.setText(
                        "❌ Firebase से Admin जानकारी नहीं मिली:\n\n" +
                        e.getMessage());
            });
                 }
    // =========================
// ADD ADMIN
// =========================

void addAdmin() {

    screen("➕ नया Admin जोड़ें");

    EditText uidInput =
            new EditText(this);

    uidInput.setHint(
            "नए अधिकारी का Firebase UID");

    uidInput.setInputType(
            InputType.TYPE_CLASS_TEXT);

    body.addView(uidInput);

    EditText nameInput =
            new EditText(this);

    nameInput.setHint(
            "अधिकारी का नाम");

    body.addView(nameInput);

    Button saveButton =
            button("💾 Admin सेव करें");

    body.addView(saveButton);

    Button backButton =
            button("⬅️ Super Admin Panel");

    body.addView(backButton);

    status = text("", 16);

    body.addView(status);

    backButton.setOnClickListener(
            v -> superAdminPanel());

    saveButton.setOnClickListener(v -> {

        String uid =
                uidInput.getText()
                        .toString()
                        .trim();

        String name =
                nameInput.getText()
                        .toString()
                        .trim();

        if (uid.isEmpty()) {

            status.setText(
                    "Firebase UID डालें");
            return;
        }

        if (name.isEmpty()) {

            status.setText(
                    "अधिकारी का नाम डालें");
            return;
        }

        Map<String, Object> adminData =
                new HashMap<>();

        adminData.put(
                "name",
                name);

        adminData.put(
                "role",
                "admin");

        adminData.put(
                "active",
                true);

        db.collection("admins")
                .document(uid)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {

                    status.setText(
                            "✅ Admin सफलतापूर्वक जोड़ दिया गया\n\n" +
                            "नाम: " +
                            name +
                            "\n\nUID:\n" +
                            uid);
                })
                .addOnFailureListener(e -> {

                    status.setText(
                            "❌ Admin नहीं जोड़ा गया:\n" +
                            e.getMessage());
                });
    });
}
    // =========================
// MANAGE ADMINS
// =========================

void manageAdmins() {

    screen("👥 Admin की सूची / प्रबंधन");

    status = text(
            "Admin की सूची लोड हो रही है...",
            16);

    body.addView(status);

    Button backButton =
            button("⬅️ Super Admin Panel");

    body.addView(backButton);

    backButton.setOnClickListener(
            v -> superAdminPanel());

    db.collection("admins")
            .get()
            .addOnSuccessListener(snapshot -> {

                status.setText(
                        "कुल Admin: " +
                        snapshot.size());

                for (DocumentSnapshot doc :
                        snapshot.getDocuments()) {

                    String uid =
                            doc.getId();

                    String name =
                            doc.getString("name");

                    String role =
                            doc.getString("role");

                    Boolean active =
                            doc.getBoolean("active");

                    if (name == null)
                        name = "नाम नहीं है";

                    if (role == null)
                        role = "admin";

                    if (active == null)
                        active = false;

                    TextView adminInfo =
                            text(
                                    "👤 " + name +
                                    "\n🔑 Role: " + role +
                                    "\n🆔 UID: " + uid +
                                    "\n📌 स्थिति: " +
                                    (active
                                            ? "सक्रिय"
                                            : "बंद"),
                                    15);

                    body.addView(adminInfo);

                    Button toggleButton =
                            button(
                                    active
                                            ? "🚫 Admin बंद करें"
                                            : "✅ Admin चालू करें");

                    body.addView(toggleButton);

                    Boolean currentActive =
                            active;

                    toggleButton.setOnClickListener(
                            v -> {

                                db.collection("admins")
                                        .document(uid)
                                        .update(
                                                "active",
                                                !currentActive)
                                        .addOnSuccessListener(
                                                aVoid -> {

                                                    manageAdmins();
                                                })
                                        .addOnFailureListener(
                                                e -> {

                                                    Toast.makeText(
                                                            this,
                                                            "❌ बदलाव नहीं हुआ",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                });
                            });
                }
            })
            .addOnFailureListener(e -> {

                status.setText(
                        "❌ Admin सूची लोड नहीं हुई:\n" +
                        e.getMessage());
            });
        }
    @Override
protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data) {

    super.onActivityResult(
            requestCode,
            resultCode,
            data);

    if (requestCode == 1001 &&
        resultCode == RESULT_OK &&
        data != null) {

    selectedImageUri =
            data.getData();

    Toast.makeText(
            this,
            "✅ फोटो चुन ली गई",
            Toast.LENGTH_SHORT)
            .show();
}

if (requestCode == 2001 &&
        resultCode == RESULT_OK &&
        data != null) {

    solutionImageUri =
            data.getData();

    Toast.makeText(
            this,
            "✅ समाधान की फोटो चुन ली गई",
            Toast.LENGTH_SHORT)
            .show();
}
 }
    // =========================
    // PHONE BACK BUTTON

    @Override
    public void onBackPressed() {

        if (auth.getCurrentUser() != null) {
            home();
        } else {
            login();
        }
    }
}   
