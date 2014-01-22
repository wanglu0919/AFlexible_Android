package com.intertid.aflexible;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.intertid.aflexible.entity.Channel;
import com.intertid.aflexible.exception.JsonPaseException;
import com.intertid.aflexible.http.entityhandler.JsonPaser;

public class MyJsonEntityHandler extends JsonPaser {

	@Override
	public Object parseResponse(String data)  {
		List<Channel> channels = new ArrayList<Channel>();
		JSONObject jreslut = null;

		try {
			jreslut = new JSONObject(data);
			JSONArray jchanlles = jreslut.getJSONArray("result");

			for (int index = 0; index < jchanlles.length(); index++) {
				JSONObject jb = jchanlles.getJSONObject(index);
				Channel channel = new Channel();
				channel.setChannelId(jb.getString("channelId"));
				channel.setContents(jb.getString("contents"));
				channels.add(channel);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new JsonPaseException(e.getMessage());
			
		}

		

		return channels;
	}

}
