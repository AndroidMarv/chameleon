package com.knolskape.chameleon.thememanager;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.knolskape.chameleon.Lex.Lex;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Created by omkar on 24/5/17.
 */

public class ThemeManager {

  Context context;
  Lex lex;
  JsonElement rulesJson;

  public ThemeManager(Context context){
    this.context = context;
  }

  public static ThemeManager build(Context context){

    ThemeManager manager = new ThemeManager(context);

    Gson gson = new Gson();
    try{
      InputStream is = context.getAssets().open("sample_theme_1.json");
      int size = is.available();
      byte[] buffer = new byte[size];
      is.read(buffer);
      is.close();
      String jsonString = new String(buffer, "UTF-8");
      manager.rulesJson = gson.fromJson(jsonString, JsonElement.class);
      Log.d("Success", jsonString);

    }catch (IOException e){
      Log.e("Error in opening file", e.getMessage(), e);
    }

    return manager;
  }



  public void applyTheme(ViewGroup view){
    int numOfViews = view.getChildCount();
    applyTheme((View) view);
    View childView = null;

    for(int i=0; i<numOfViews; i++){
      childView = view.getChildAt(i);
      if(childView instanceof ViewGroup){
        applyTheme((ViewGroup) childView);
      }else{
        applyTheme(childView);
      }

    }
  }



  public void applyTheme(View view){

    Object tagObj = view.getTag();
    if(tagObj == null){
      return;
    }

    String tag = null;
    if(tagObj instanceof String){
      tag = (String) tagObj;
    }else if(tagObj instanceof CTag){
      tag = ((CTag) tagObj).first();
    }

    if(tag != null){
      JsonElement rule = rulesJson.getAsJsonObject().get(tag);
      applyDrawableStyles(rule, view);
      applyTextStyles(rule, view);
    }
  }

  private void applyDrawableStyles(JsonElement ruleObj, View view){
    Set<Map.Entry<String, JsonElement>> rules = ruleObj.getAsJsonObject().entrySet();
    CDrawable drawable = CDrawable.build(context);
    for(Map.Entry<String, JsonElement> entry: rules){
      String ruleKey = entry.getKey();
      String ruleValue  = entry.getValue().getAsString();
      Log.d("key", ruleKey);
      Log.d("value", ruleValue);
      if(ruleKey.equals("borderRadius")){
        if(isNumeric(ruleValue)){
          drawable = drawable.withCornerRadius(Integer.parseInt(ruleValue));
        }
      }
      if(ruleKey.equals("borderWidth")){
        if(isNumeric(ruleValue)){
          JsonElement borderColorValue = ruleObj.getAsJsonObject().get("borderColor");
          if(!(borderColorValue instanceof JsonNull)){
            String borderColor = borderColorValue
                .getAsString();
            drawable.setStroke(Integer.parseInt(ruleValue), Color.parseColor(borderColor));
          }
        }
      }
      if(ruleKey.equals("bgColor")){
        drawable = handleBgColor(drawable, ruleValue);
      }
      if(ruleKey.equals("tint")){
        drawable.setTint(Color.parseColor(ruleValue));
      }
      if(ruleKey.equals("bgGradientColors")){
        drawable = handleGradientColors(drawable, ruleValue);
      }
      if(ruleKey.equals("bgGradientType")){
        drawable = drawable.withGradientType(ruleValue);
      }
      view.setBackground(drawable);
    }
  }

  private void applyTextStyles(JsonElement ruleObj, View view){
    Set<Map.Entry<String, JsonElement>> rules = ruleObj.getAsJsonObject().entrySet();
    if(view instanceof TextView){
      for(Map.Entry<String, JsonElement> entry: rules){
        String ruleKey = entry.getKey();
        String ruleValue  = entry.getValue().getAsString();
        Log.d("key", ruleKey);
        Log.d("value", ruleValue);
        if(ruleKey.equals("textSize")){
          if(isNumeric(ruleValue)){
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(ruleValue));
          }
        }
        if(ruleKey.equals("textColor")){
          ((TextView) view).setTextColor(Color.parseColor(ruleValue));
        }
      }
    }
  }

  public static boolean isNumeric(String str)
  {
    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
  }

  public CDrawable handleBgColor(CDrawable drawable, String ruleValue){
    return drawable.withColor(Color.parseColor(ruleValue));
  }

  public CDrawable handleGradientColors(CDrawable drawable, String ruleValue){
    String[] colorStrings = ruleValue.split(",");
    int[] colorArray = new int[colorStrings.length];
    int i=0;
    for(String colorString: colorStrings){
      colorArray[i++] = Color.parseColor(colorString.trim());
    }
    return drawable.withColors(colorArray);
  }
}