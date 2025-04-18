package com.example.numberbook;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.numberbook.classes.Contact;

public class ContactActionDialog extends Dialog {

    private final Contact contact;
    private final Context context;

    public ContactActionDialog(@NonNull Context context, Contact contact) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.contact = contact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_item, null);
        setContentView(view);
        
        // Make dialog background transparent
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Set animation
        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        
        // Set width to match parent
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(layoutParams);
        
        // Set contact name
        TextView contactNameTextView = findViewById(R.id.contact_name_dialog);
        contactNameTextView.setText(contact.getNom());
        
        // Set up call button
        Button callButton = findViewById(R.id.btn_call);
        callButton.setOnClickListener(v -> {
            makePhoneCall();
            dismiss();
        });
        
        // Set up SMS button
        Button smsButton = findViewById(R.id.btn_sms);
        smsButton.setOnClickListener(v -> {
            sendSMS();
            dismiss();
        });
    }
    
    private void makePhoneCall() {
        String phoneNumber = contact.getNumero().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(intent);
    }
    
    private void sendSMS() {
        String phoneNumber = contact.getNumero().trim();
        if (phoneNumber.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + phoneNumber));
        context.startActivity(intent);
    }
}
