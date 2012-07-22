/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package edu.berkeley.boinc.rpc;

public class Message {
	public static final int MSG_INFO =1;
	public static final int MSG_USER_ALERT = 2;
	public static final int MSG_INTERNAL_ERROR = 3;
	// internally used by client
	public static final int MSG_SCHEDULER_ALERT = 4;
	
	public String project = "";
	public int    priority;
	public int    seqno;
	public long   timestamp;
	public String body;
}
