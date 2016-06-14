/*
 * Created on 14-Jun-2005
 * Created by Paul Gardner
 * Copyright (C) Azureus Software, Inc, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.aelitis.azureus.plugins.dht.impl;

import com.aelitis.azureus.core.dht.transport.DHTTransportValue;
import com.aelitis.azureus.plugins.dht.DHTPluginValue;


public class
DHTPluginValueImpl
	implements DHTPluginValue
{
	private DHTTransportValue		value;
	
	protected
	DHTPluginValueImpl(
		DHTTransportValue	_value )
	{
		value	= _value;
	}
	
	public byte[]
	getValue()
	{
		return( value.getValue());
	}
	
	public long
	getCreationTime()
	{
		return( value.getCreationTime());
	}
	
	public long
	getVersion()
	{
		return( value.getVersion());
	}
	
	public boolean 
	isLocal() 
	{
		return( value.isLocal());
	}
	
	public int
	getFlags()
	{
		return( value.getFlags()&0xff);
	}
}