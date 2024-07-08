/*************************************************************************************
* JKeyInfo.java        : SSP Java Interface file for defining class used in GetKeyInfo.
*
* Developed by         : SafeNet,Inc
*
* (C) Copyright 2007 SafeNet, Inc. All Rights Reserved.
*
*************************************************************************************/
package softtest.registery;
public class JKeyInfo
{
    public int devId;
    public int hrdLmt;
    public int inUse;
    public int numTimedOut;
    public int highestUse;

    public void JkeyInfo()
    {
       devId       = 0;
       hrdLmt      = 0;
       inUse       = 0;
       numTimedOut = 0;
       highestUse  = 0;
    }
}
