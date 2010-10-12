/**
 * 
 */
package com.nanosheep.bikeroute.service;

import org.andnav.osm.util.GeoPoint;

import com.nanosheep.bikeroute.constants.BikeRouteConsts;
import com.nanosheep.bikeroute.utility.Parking;
import com.nanosheep.bikeroute.utility.RouteManager;
import com.nanosheep.bikeroute.utility.Stands;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;

/**
 * Search task, 
 * Displays a planning dialog, searches, then transitions to a map displaying the
 * located route if one is found and adds start & destination to a db of recently used
 * addresses, displays an error if planning failed.
 * @author jono@nanosheep.net
 * @version Oct 11, 2010
 */

public class RoutePlannerTask extends AsyncTask<Void, Void, Integer> {
	/** Route planner service consts. **/
	/** Request name string. **/
	public static final String PLAN_TYPE = "plan_type";
	/** Bike plan. **/
	public static final int BIKE_PLAN = 0;
	/** GeoPoint plan. **/
	public static final int GEOPOINT_PLAN = 1;
	/** Replanning request. **/
	public static final int REPLAN_PLAN = 2;
	/** Stand plan. **/
	public static final int STANDS_PLAN = 3;
	/** Address plan. **/
	public static final int ADDRESS_PLAN = 4;
	public static final String START_ADDRESS = "start_address";
	public static final String END_ADDRESS = "end_address";
	public static final String START_LOCATION = "start_location";
	public static final String INTENT_ID = "com.nanosheep.bikeroute.service.RoutePlannerService";
	public static final String END_POINT = "end_point";
	private RouteManager planner;
    protected String startAddressInput;
    protected String endAddressInput;
	private RouteListener mAct;
	private Intent mIntent;
        
        
        public RoutePlannerTask(RouteListener act, Intent intent) {
                super();
                mIntent = intent;
                mAct = act;
                planner = new RouteManager(mAct.getContext());
        }
        
        @Override
        protected void onPreExecute() {
                
        }
        @Override
        protected Integer doInBackground(Void... arg0) {
        	int msg = BikeRouteConsts.PLAN_FAIL_DIALOG; 
    		final String startAddressInput = mIntent.getStringExtra(START_ADDRESS);
    		final String endAddressInput = mIntent.getStringExtra(END_ADDRESS);
                switch(mIntent.getIntExtra(PLAN_TYPE, ADDRESS_PLAN)) {
        		case ADDRESS_PLAN:
        			if ("".equals(startAddressInput) || "".equals(endAddressInput)) {
        				msg = BikeRouteConsts.ARGERROR;
        			} else {
        				msg = BikeRouteConsts.RESULT_OK;
        				try {
        					planner.setStart(startAddressInput);
        					planner.setDest(endAddressInput);		
        				} catch (Exception e) {
        					msg = BikeRouteConsts.IOERROR;
        				}
        			}
        			break;
        		case BIKE_PLAN:
        			final Parking prk = new Parking(mAct.getContext());
        			if ("".equals(startAddressInput)) {
        				msg = BikeRouteConsts.ARGERROR;
        			} else {
        				try {
        					planner.setStart(startAddressInput);
        					planner.setDest(prk.getLocation());	
        				} catch (Exception e) {
        					msg = BikeRouteConsts.IOERROR;
        				}
        			}
        			break;
        		case STANDS_PLAN:
        			if ("".equals(startAddressInput)) {
        				msg = BikeRouteConsts.ARGERROR;
        			} else {
        				msg = BikeRouteConsts.RESULT_OK;
        				try {
        					planner.setStart(startAddressInput);
        					planner.setDest(Stands.getNearest(planner.getStart(), mAct.getContext()));	
        				} catch (Exception e) {
        					msg = BikeRouteConsts.IOERROR;
        				}
        			}
        			break;
        		case REPLAN_PLAN:
        			final Location start = mIntent.getParcelableExtra(START_LOCATION);
        			final GeoPoint dest = mIntent.getParcelableExtra(END_POINT);
        			msg = BikeRouteConsts.RESULT_OK;
        			planner.setStart(start);
        			planner.setDest(dest);	
        			break;
        		default:
        			msg = BikeRouteConsts.PLAN_FAIL_DIALOG;
        		}
                try {
                	if ((msg == BikeRouteConsts.RESULT_OK) && !planner.showRoute()) {
                		msg = BikeRouteConsts.PLAN_FAIL_DIALOG;
                	}
        		} catch (Exception e) {
        			msg = BikeRouteConsts.IOERROR;
        		}
        		return msg;
        }
        @Override
        protected void onPostExecute(final Integer msg) {
        	if (msg != null){
        		mAct.searchComplete(msg, planner.getRoute());
        	}
        }
        
        @Override
        protected void onCancelled() {
        	mAct.searchCancelled();
        	super.onCancelled();
        }
}
