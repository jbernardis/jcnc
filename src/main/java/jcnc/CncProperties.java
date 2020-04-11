package main.java.jcnc;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CncProperties {
	JSONObject cncProps;
	
	public CncProperties() {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader("shapeoko.json")) {
            cncProps = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            cncProps = new JSONObject();
            setDefaultProperties();
        } catch (ParseException e) {
            e.printStackTrace();
        }
	}
	
	@SuppressWarnings("unchecked")
	private void setDefaultProperties() {
		cncProps.put("addspeed", true);
		cncProps.put("metric", true);
		cncProps.put("lastgcodedir", "C:\\");
		cncProps.put("lastjsondir", "C:\\");
		saveProperties();
	}
	
	public boolean getPropMetric() {
		return (boolean) cncProps.get("metric");
	}
	
	@SuppressWarnings("unchecked")
	public void setPropMetric(boolean v) {
		cncProps.put("metric", v);
		saveProperties();
	}
		
	public boolean getPropAddSpeed() {
		return (boolean) cncProps.get("addspeed");
	}
	
	@SuppressWarnings("unchecked")
	public void setPropAddSpeed(boolean v) {
		cncProps.put("addspeed", v);
		saveProperties();
	}
	
	public String getPropLastGCodeDir() {
		return (String) cncProps.get("lastgcodedir");
	}
	
	@SuppressWarnings("unchecked")
	public void setPropLastGCodeDir(String d) {
		cncProps.put("lastgcodedir",  d);
		saveProperties();
	}
	
	public String getPropLastJSONDir() {
		return (String) cncProps.get("lastjsondir");
	}
	
	@SuppressWarnings("unchecked")
	public void setPropLastJSONDir(String d) {
		cncProps.put("lastjsondir",  d);
		saveProperties();
	}
	
	public String getCurrentTool() {
		return (String) cncProps.get("currenttool");
	}
	
	@SuppressWarnings("unchecked")
	public void setCurrentTool(String tool) {
		cncProps.put("currenttool", tool);
		saveProperties();
	}
	
	public String getCurrentMaterial() {
		return (String) cncProps.get("currentmaterial");
	}
	
	@SuppressWarnings("unchecked")
	public void setCurrentMaterial(String material) {
		cncProps.put("currentmaterial", material);
		saveProperties();
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getToolList() {
		List<String> tl = new ArrayList<String>();
		tl.clear();
		HashMap<String,Object> ptl = (HashMap<String, Object>) cncProps.get("tools");
		for (String key : ptl.keySet()) {
			tl.add(key);
		}
		return tl;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getTool(String tid) {
		HashMap<String,Object> ptl = (HashMap<String, Object>) cncProps.get("tools");
		if (tid != null) {
			if (!ptl.containsKey(tid))
				return null;
		
			return (HashMap<String, Object>) ptl.get(tid);
		}
		else {
			String ct = getCurrentTool();
			return (HashMap<String, Object>) ptl.get(ct);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getMaterialList() {
		List<String> tl = new ArrayList<String>();
		tl.clear();
		List<String> ptl = (List<String>) cncProps.get("materials");
		for (String key : ptl) {
			tl.add(key);
		}
		return tl;
	}
	
	private void saveProperties() {
		Writer writer = new JSONWriter(); 
		try {
			cncProps.writeJSONString(writer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter myWriter = new FileWriter("shapeoko.json");
			myWriter.write(writer.toString());
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An IO error occurred.");
			e.printStackTrace();
		}
	}
}
