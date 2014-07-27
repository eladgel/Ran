package com.danbt.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.danbt.R;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> names;

	static class ViewHolder {
		public TextView text;
		public TextView symbol;
		public TextView data;
	}

	public MySimpleArrayAdapter(Context context, ArrayList<String> values) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.names = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.rowlayout, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) rowView.findViewById(R.id.label);
			viewHolder.symbol = (TextView) rowView.findViewById(R.id.symbol);
			viewHolder.data = (TextView) rowView.findViewById(R.id.data);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		String s = names.get(position);

		if (s.startsWith("c")) {
			s = s.substring(1);
			holder.symbol.setText(Html.fromHtml("<small>" + s + "</small>"));
			holder.text.setText("");
			holder.data.setText("");
		} else {

			String x = s.substring(0, 1);
			s = s.substring(2);
			String y = "";
			if (!s.startsWith(".")) {
				y = s.substring(0, 1);
			}
			s = s.substring(2);
			String d = s.substring(0, s.indexOf("q"));
			s = s.substring(s.indexOf("q") + 1);
			holder.symbol.setText(Html.fromHtml("<i>" + x
					+ "<sub><small><small><small>" + y
					+ "</small></small></small></sub></i>"));
			holder.text.setText(Html.fromHtml(s));
			holder.data.setText(Html.fromHtml(d));
		}

		return rowView;
	}
}