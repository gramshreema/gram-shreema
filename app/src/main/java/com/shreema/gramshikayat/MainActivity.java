package com.shreema.gramshikayat;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.text.InputType;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

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

        screen("जन शिकायत एवं समाधान");

        body.addView(
                text(
                        "आपकी समस्या, हमारी जिम्मेदारी",
                        20));

        Button complaintButton =
                button("📝 शिकायत दर्ज करें");

        Button trackButton =
                button("🔎 शिकायत की स्थिति");

        Button mineButton =
                button("📋 मेरी शिकायतें");
        Button adminButton =
        button("🛠️ Admin Panel");

        Button logoutButton =
                button("🚪 लॉगआउट");

        body.addView(complaintButton);
        body.addView(trackButton);
        body.addView(mineButton);
        body.addView(adminButton);
        body.addView(logoutButton);

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
                            "त्रुटि:\n" +
                            e.getMessage());
                });
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
    }

    // =========================
    // MY COMPLAINTS
    // =========================

    void mine() {

        screen("मेरी शिकायतें");

        status =
                text(
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

                    for (
                            DocumentSnapshot d :
                            result.getDocuments()) {

                        String info =
                                "Complaint ID:\n" +
                                d.getId() +
                                "\n\nश्रेणी: " +
                                d.getString("category") +
                                "\nस्थिति: " +
                                d.getString("status") +
                                "\n\n";

                        body.addView(
                                text(info, 17));

                        Button view =
                                button(
                                        "🔎 स्थिति देखें");

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
                    return;
                }

                db.collection("complaints")
                        .get()
                        .addOnSuccessListener(result -> {

                            status.setText("");

                            int activeCount = 0;

                            for (
                                    DocumentSnapshot d :
                                    result.getDocuments()) {

                                String currentStatus =
                                        d.getString("status");

                                if ("निस्तारित".equals(
                                        currentStatus)) {
                                    continue;
                                }

                                activeCount++;

                                String info =
                                        "Complaint ID:\n" +
                                        d.getId() +
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

                                body.addView(callButton);
                                String phoneNumber =
        d.getString("phone");

callButton.setOnClickListener(v -> {

    if (phoneNumber != null &&
            !phoneNumber.trim().isEmpty()) {

        android.content.Intent intent =
                new android.content.Intent(
                        android.content.Intent.ACTION_DIAL);

        intent.setData(
                android.net.Uri.parse(
                        "tel:" + phoneNumber));

        startActivity(intent);

    } else {

        Toast.makeText(
                this,
                "मोबाइल नंबर उपलब्ध नहीं है",
                Toast.LENGTH_SHORT).show();
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

                            if (activeCount == 0) {

                                status.setText(
                                        "✅ सभी शिकायतों का निस्तारण हो चुका है।");
                            }
                        })
                        .addOnFailureListener(e -> {

                            status.setText(
                                    "शिकायतें लोड नहीं हुईं:\n" +
                                    e.getMessage());
                        });
            })
            .addOnFailureListener(e -> {

                status.setText(
                        "Admin जाँच में समस्या:\n" +
                        e.getMessage());
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
    });
    }
    // =========================
    // PHONE BACK BUTTON
    // =========================

    @Override
    public void onBackPressed() {

        if (auth.getCurrentUser() != null) {
            home();
        } else {
            login();
        }
    }
}   
