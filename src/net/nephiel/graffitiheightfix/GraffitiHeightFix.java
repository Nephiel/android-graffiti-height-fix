package net.nephiel.graffitiheightfix;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class GraffitiHeightFix implements IXposedHookLoadPackage {
	
	private static final String GRAFFITI_PKGNAME = "com.access_company.graffiti";
	private static final String GRAFFITI_PRO_PKGNAME = "com.access_company.graffiti_pro";
	
	/* 
	 * Consider a 480x800 px screen where Graffiti area is 232 px high,
	 * use the same aspect ratio for other resolutions.
	 */
	private static final double ASPECT_RATIO_PORTRAIT = 3.448;  // (800/232) (approx.)
	private static final double ASPECT_RATIO_LANDSCAPE = 2.069; // (480/232) (approx.)
	
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
   		if (!lpparam.packageName.equals(GRAFFITI_PKGNAME))
   	   		if (!lpparam.packageName.equals(GRAFFITI_PRO_PKGNAME))
   	   			return;

   	    //XposedBridge.log("GHF: Graffiti Height Fix loaded");
   	    
   	    XC_MethodHook setMeasuredDimensionHook = new XC_MethodHook()  {
   	    	@Override
   	    	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
   	    	    if ( (param.thisObject.getClass().getName().equals(GRAFFITI_PKGNAME + ".GraffitiView"))
   	    	      || (param.thisObject.getClass().getName().equals(GRAFFITI_PRO_PKGNAME + ".GraffitiView")) ) {
   	    	    	
   	    	    	//int requestedWidth = (Integer) param.args[0];
   	    	    	//int requestedHeight = (Integer) param.args[1];
   	    	    	//XposedBridge.log("GHF: requested " + requestedWidth + "x" + requestedHeight);
   	    	    	
   	    	    	View view = (View) param.thisObject;
   	    	    	WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
   	    	    	Display display = wm.getDefaultDisplay();
   	    	    	Point screenSize = new Point();
   	    	    	display.getSize(screenSize);
   	    	    	int screenWidth = screenSize.x;
   	    	    	int screenHeight = screenSize.y;
   	    	    	//XposedBridge.log("GHF: screen is " + screenWidth + "x" + screenHeight);
   	    	    	
   	    	    	if (screenHeight > screenWidth) // portrait
   	    	    		param.args[1] = (int) (screenHeight / ASPECT_RATIO_PORTRAIT);
   	    	    	else // landscape
   	    	    		param.args[1] = (int) (screenHeight / ASPECT_RATIO_LANDSCAPE);
   	    	    	//XposedBridge.log("GHF: returning " + requestedWidth + "x" + param.args[1]);
   	    	    }
   	    	}
   	    };

        findAndHookMethod("android.view.View",
            	lpparam.classLoader, 
           		"setMeasuredDimension", int.class, int.class, 
           		setMeasuredDimensionHook);
	}
}
