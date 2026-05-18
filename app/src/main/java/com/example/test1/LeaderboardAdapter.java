package com.example.test1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class LeaderboardAdapter extends BaseAdapter {
    private final Context context;
    private final List<Map<String, String>> data;
    private final String currentUserId;

    public LeaderboardAdapter(Context context, List<Map<String, String>> data, String currentUserId) {
        this.context = context;
        this.data = data;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Map<String, String> item = data.get(position);
        String userId = item.get("userId");
        String nickname = item.get("nickname");
        String totalSteps = item.get("totalSteps");

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(nickname);
        text2.setText(totalSteps);

        // 현재 로그인한 사용자의 항목 배경색 변경
        if (userId.equals(currentUserId)) {
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        return convertView;
    }
}
