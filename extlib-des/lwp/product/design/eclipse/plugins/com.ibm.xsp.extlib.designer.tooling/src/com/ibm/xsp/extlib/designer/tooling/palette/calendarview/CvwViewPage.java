/*
 * � Copyright IBM Corp. 2014, 2016
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.xsp.extlib.designer.tooling.palette.calendarview;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import com.ibm.commons.iloader.node.DataChangeListener;
import com.ibm.commons.iloader.node.DataNode;
import com.ibm.commons.iloader.node.IClassDef;
import com.ibm.commons.iloader.node.ILoader;
import com.ibm.commons.iloader.node.IMember;
import com.ibm.commons.iloader.node.NodeException;
import com.ibm.commons.iloader.node.collections.SingleCollection;
import com.ibm.commons.swt.SWTLayoutUtils;
import com.ibm.commons.swt.SWTUtils;
import com.ibm.commons.swt.data.controls.DCPanel;
import com.ibm.commons.swt.data.controls.DCUtils;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.constants.XSPAttributeNames;
import com.ibm.designer.domino.constants.XSPTagLibs;
import com.ibm.designer.domino.constants.XSPTagNames;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.xsp.api.panels.PanelExtraData;
import com.ibm.designer.domino.xsp.api.panels.complex.ComplexPanelComposite;
import com.ibm.designer.domino.xsp.api.util.XPagesDOMUtil;
import com.ibm.designer.domino.xsp.api.util.XPagesPropertiesViewUtils;
import com.ibm.designer.domino.xsp.dominoutils.DominoImportException;
import com.ibm.designer.domino.xsp.dominoutils.DominoUtil;
import com.ibm.xsp.extlib.designer.tooling.utils.AbstractWizardPage;
import com.ibm.xsp.extlib.designer.tooling.utils.ExtLibToolingLogger;
import com.ibm.xsp.extlib.designer.tooling.utils.WizardUtils;

/**
 * @author Gary Marjoram
 *
 */
public class CvwViewPage extends AbstractWizardPage implements DataChangeListener{
    
    private PanelExtraData      _panelData      = null;
    private Element             _dominoViewNode = null;

    public CvwViewPage(String pageName) {
        super(pageName);
    }

    @Override
    protected String getPageTitle() {
        return "Calendar REST Service"; // $NLX-CvwViewPage.CalendarRESTService-1$
    }

    @Override
    protected String getPageMsg() {
        return "Choose the view containing the calendar data you want to use"; // $NLX-CvwViewPage.Choosetheviewcontainingthecalenda-1$
    }
    
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        
        _panelData = ((CalendarViewDropWizard) this.getWizard()).getPanelData();
        
        // Create the main panel
        DCPanel _mainPanel = new DCPanel(parent, SWT.NONE);
        _mainPanel.setLayout(SWTLayoutUtils.createLayoutDefaultSpacing(1));
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        _mainPanel.setLayoutData(data);

        // Initialise the data for the dynamic panel
        initData(_mainPanel);
        
        // Create the "Choose View" panel
        new ComplexPanelComposite(_mainPanel, SWT.NONE,
                "http://www.ibm.com/xsp/coreex/wizard/panels", "chooseCalendarView", // $NON-NLS-1$ $NON-NLS-2$
                _panelData).setLayoutData(GridDataFactory.copyData(data));
        SWTUtils.setBackgroundColor(_mainPanel, parent.getBackground(), true);

        setControl(_mainPanel);
        setPageComplete(true);
    }
    
    /*
     * Function to initialise the back-end data node and element for the
     * "Choose View" dynamic panel
     */
    private void initData(Composite parent) {
        // Find the data node
        DataNode dn = DCUtils.findDataNode(parent, true); 
        if (dn != null) {
            // Use a dummy dominoView as the element for the "Choose View" panel
            IClassDef classDef;
            try {
                // Get the dominoView class definition
                ILoader xpagesDOMLoader = XPagesPropertiesViewUtils.getXPagesMultiDomLoader(_panelData.getDesignerProject());
                classDef = xpagesDOMLoader.loadClass(XSPTagLibs.XSP_NAMESPACE_URI, XSPTagNames.XSP_TAG_DATASOURCE_DOMINO_VIEW);
            } catch (NodeException e) {
                if(ExtLibToolingLogger.EXT_LIB_TOOLING_LOGGER.isErrorEnabled()){
                    String msg = "Failed to load dataView class"; // $NLE-CvwViewPage.FailedtoloaddataViewclass-1$
                    ExtLibToolingLogger.EXT_LIB_TOOLING_LOGGER.errorp(this, "initData", e, msg); // $NON-NLS-1$
                }
                return;
            }
            
            // Set the data node class definition
            dn.setClassDef(classDef);
            try {
                // Create the dummy dominoView element
                _dominoViewNode = (Element) classDef.newInstance(XPagesDOMUtil.getViewNode(_panelData.getDocument()).cloneNode(false));  
    
                // Link the data node to the dummy dominoView
                dn.setDataProvider(new SingleCollection(_dominoViewNode));
                
                // Listen for changes
                dn.getDataChangeNotifier().addDataChangeListener(this);
            } catch (NodeException e) {
                if(ExtLibToolingLogger.EXT_LIB_TOOLING_LOGGER.isErrorEnabled()){
                    String msg = "Failed to create dummy dataView element"; // $NLE-CvwViewPage.FailedtocreatedummydataViewelemen-1$
                    ExtLibToolingLogger.EXT_LIB_TOOLING_LOGGER.errorp(this, "initData", e, msg); // $NON-NLS-1$
                }
                return;
            }
        }
    }    
    
    public String getFullDbName() {
        return StringUtil.getNonNullString(getDominoViewAttr(XSPAttributeNames.XSP_ATTR_DATABASE_NAME));
    }
    
    public String getDbName() {
        String fullDbName = getFullDbName();
        if (fullDbName.contains("!!")) {
            return fullDbName.split("!!")[1];
        }
        
        return fullDbName;
    }

    public String getServerName() {
        String fullDbName = getFullDbName();
        if (fullDbName.contains("!!")) {
            return fullDbName.split("!!")[0];
        }
        
        return "";
    }

    public String getViewName() {
        return getDominoViewAttr(XSPAttributeNames.XSP_ATTR_VIEW_NAME);
    }
    
    private String getDominoViewAttr(String attrName) {
        if (_dominoViewNode != null) {
            return _dominoViewNode.getAttribute(attrName);
        }
        
        return "";        
    }
    
    //
    // Gets column Titles and Names for the selected View
    //
    public String [][] getViewColumns() {
        String dbName = getFullDbName();

        DesignerProject prj = _panelData.getDesignerProject();
        if (StringUtil.isEmpty(dbName)) {
            if (prj != null) {
                dbName = prj.getDatabaseName();
            }
        }
        if (StringUtil.isEmpty(dbName)) {
            return null;
        }
        String serverName = prj != null ? prj.getServerName() : null;
        if (StringUtil.isEmpty(serverName)) {
            serverName = DominoUtil.LOCAL_CLIENT;
        }
        
        // Check if the dbName contains a server
        if (dbName.contains(DominoUtil.DB_SERVER_SEPARATOR)) {
            serverName = dbName.substring(0, dbName.indexOf(DominoUtil.DB_SERVER_SEPARATOR));
            dbName = dbName.substring(dbName.indexOf(DominoUtil.DB_SERVER_SEPARATOR) + 2);
        }
        String viewName = getViewName();
        if (StringUtil.isEmpty(viewName)) {
            return null;
        }

        try {
            return WizardUtils.getViewColumns(serverName, dbName, viewName, false, false);
        }
        catch (DominoImportException e) {
            if(ExtLibToolingLogger.EXT_LIB_TOOLING_LOGGER.isErrorEnabled()){                        
                ExtLibToolingLogger.EXT_LIB_TOOLING_LOGGER.error(e, e.toString());
            }
        }
        
        return null;
    }
    
    @Override
    public void onValueChanged(Object object, int operation, IMember member, int position) {
        setHasChanged(true);
    }
}