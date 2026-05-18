package com.example.test1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context context;
    private List<Message> messages;

    public ChatAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.txtMessage.setText(message.getText());
        holder.txtSender.setText(message.getSender());
        Log.d("ChatAdapter", "Binding message: " + message.getText() + ", userId: " + message.getUserId());
        holder.btnProfile.setOnClickListener(v->{
            String userId = message.getUserId(); // Firestore에서 가져온 userId
            if (userId == null) {
                Log.e("ChatAdapter", "UserId is null at position: " + position);
                return;
            }

            // Context 확인
            if (context instanceof FragmentActivity) {
                ProfileBottomSheet bottomSheet = new ProfileBottomSheet(userId);
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .add(bottomSheet, "ProfileBottomSheet")
                        .commitAllowingStateLoss();
            } else {
                Log.e("ChatAdapter", "Invalid context for FragmentManager");
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtSender;
        ImageButton btnProfile;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtSender = itemView.findViewById(R.id.txt_sender);
            btnProfile=itemView.findViewById(R.id.btn_profile);
        }
    }
}
