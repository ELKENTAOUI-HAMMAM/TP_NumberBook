package com.example.numberbook.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.numberbook.ContactActionDialog;
import com.example.numberbook.R;
import com.example.numberbook.classes.Contact;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private List<Contact> contactList;
    private List<Contact> contactListFull;
    private Context context;
    private TextView noResultsTextView;


    public ContactAdapter(Context context, List<Contact> contactList, TextView noResultsTextView) {
        this.context = context;
        this.contactList = contactList;
        this.contactListFull = new ArrayList<>(contactList);
        this.noResultsTextView = noResultsTextView;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nameTextView.setText(contact.getNom());
        holder.phoneTextView.setText(contact.getNumero());

        // Set contact photo if available
        if (contact.getPhotoUri() != null && !contact.getPhotoUri().isEmpty()) {
            holder.photoImageView.setImageURI(Uri.parse(contact.getPhotoUri()));
        } else {
            holder.photoImageView.setImageResource(R.drawable.circle_background);
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            showContactActionDialog(contact);
        });
    }

    private void showContactActionDialog(Contact contact) {
        ContactActionDialog dialog = new ContactActionDialog(context, contact);
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateContacts(List<Contact> contacts) {
        this.contactList = contacts;
        this.contactListFull = new ArrayList<>(contacts);
        notifyDataSetChanged();

        // Update no results view
        updateNoResultsView();
    }

    private void updateNoResultsView() {
        if (contactList.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(contactListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Contact contact : contactListFull) {
                    if (contact.getNom().toLowerCase().contains(filterPattern) ||
                            contact.getNumero().toLowerCase().contains(filterPattern)) {
                        filteredList.add(contact);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactList.clear();
            contactList.addAll((List) results.values);
            notifyDataSetChanged();

            // Update no results view
            updateNoResultsView();
        }
    };

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        ImageView photoImageView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_number);
            photoImageView = itemView.findViewById(R.id.contact_photo);
        }
    }
}
