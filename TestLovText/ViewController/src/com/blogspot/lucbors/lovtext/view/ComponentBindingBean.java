package com.blogspot.lucbors.lovtext.view;

import oracle.adf.view.rich.component.rich.RichQuery;
import oracle.adf.view.rich.component.rich.input.RichInputListOfValues;

import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.render.ClientEvent;

import com.blogspot.lucbors.lovtext.view.utils.JSFUtils;

import java.util.Iterator;

import java.util.List;

import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;

import oracle.adf.view.rich.component.rich.RichDialog;
import oracle.adf.view.rich.component.rich.RichPopup;


import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.layout.RichGridCell;
import oracle.adf.view.rich.component.rich.layout.RichGridRow;
import oracle.adf.view.rich.component.rich.layout.RichPanelGridLayout;
import oracle.adf.view.rich.component.rich.layout.RichPanelGroupLayout;
import oracle.adf.view.rich.component.rich.layout.RichPanelHeader;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adf.view.rich.event.LaunchPopupEvent;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.ComponentReference;
import org.apache.myfaces.trinidad.util.Service;

public class ComponentBindingBean {
    private ComponentReference listOfValues;
    private FacesContext fctx = FacesContext.getCurrentInstance();

    public ComponentBindingBean() {


    }


    public void setListOfValues(RichInputListOfValues listOfValues) {
        this.listOfValues = ComponentReference.newUIComponentReference(listOfValues);
    }

    public RichInputListOfValues getListOfValues() {


        if (listOfValues != null) {

            return (RichInputListOfValues) listOfValues.getComponent();
        }
        return null;
    }


    //method called from the server listener
    public void changeLovInternals(ClientEvent ce) {
        //get the LOV component binding reference
        RichInputListOfValues lov = getListOfValues();
        String id = lov.getClientId(fctx);
        RichPopup popup = null;
        String facetName = null;
        UIComponent componentFacet = null;
        
        Iterator facetNames = lov.getFacetNames();
        while (facetNames.hasNext()) {
            facetName = (String) facetNames.next();
            if (facetName != null) {
                componentFacet = lov.getFacet(facetName);
                if (componentFacet != null && componentFacet instanceof RichPopup) {
                    popup = (RichPopup) componentFacet;
                    break;
                }
            }
        }
        
        RichDialog lovDialog = (RichDialog) popup.getChildren().get(0);
        
        lovDialog.setCancelTextAndAccessKey("&Take me back");
        lovDialog.setAffirmativeTextAndAccessKey("&Make my choice");
        
        //refresh the LOV to show the buttons
        AdfFacesContext.getCurrentInstance().addPartialTarget(lovDialog);
        // I found that the dialog consists of a panelGridLayout with 2 gridrows
        if (lovDialog.getChildCount() == 1 && lovDialog.getChildren().get(0) != null &&
            lovDialog.getChildren().get(0) instanceof RichPanelGridLayout) {
            RichPanelGridLayout panelGridLayout =
                (RichPanelGridLayout) lovDialog.getChildren().get(0);
            List uiCompList = panelGridLayout.getChildren();
            // always gridrows and always two.
            if (uiCompList.size() > 0) {
                // the first one is the search panel
                // the second contains the table
                RichGridRow richGridRow = (RichGridRow) uiCompList.get(1);
                RichGridCell richCell = (RichGridCell) richGridRow.getChildren().get(0);
                RichTable theTable = (RichTable) richCell.getChildren().get(0);
                theTable.setEmptyText("Really, this is something, but it works");
                AdfFacesContext.getCurrentInstance().addPartialTarget(theTable);
            }
        }

    }
    
    public void onLOVLaunch(LaunchPopupEvent launchPopupEvent) {
        ExtendedRenderKitService erks = Service.getService(fctx.getRenderKit(), ExtendedRenderKitService.class);
        //create the JavaScript and invoke it on the client. The af:form id is "f1"
        StringBuffer scriptBuf = new StringBuffer();
        scriptBuf.append("var afForm = AdfPage.PAGE.findComponentByAbsoluteId(\"f1\");");
        scriptBuf.append("AdfCustomEvent.queue(afForm,\"inputLovManipulator\",{},true);");
        erks.addScript(fctx, scriptBuf.toString());
    }

    public void lovAfterLaunch(ClientEvent clientEvent) {
        if (clientEvent.getParameters().size() > 0 && clientEvent.getParameters().get("compId") != null) {
            String compId = clientEvent.getParameters().get("compId").toString();
            RichInputListOfValues inputListOfValues = (RichInputListOfValues) JSFUtils.findComponentInRoot(compId);
            if (inputListOfValues != null) {
                Iterator facetNames = inputListOfValues.getFacetNames();
                RichPopup richPopup = null;
                String facetName = null;
                UIComponent componentFacet = null;
                UIComponent componentdialog = null;
                while (facetNames.hasNext()) {
                    facetName = (String) facetNames.next();
                    if (facetName != null) {
                        componentFacet = inputListOfValues.getFacet(facetName);
                        if (componentFacet != null && componentFacet instanceof RichPopup) {
                            richPopup = (RichPopup) componentFacet;
                            break;
                        }
                    }
                }
                if (richPopup != null) {
                    List uiCompList = richPopup.getChildren();
                    if (uiCompList.size() > 0) {
                        componentdialog = (UIComponent) uiCompList.get(0);
                        if (componentdialog != null && componentdialog instanceof RichDialog) {
                            RichDialog richDialog = (RichDialog) componentdialog;
                            if (richDialog != null) {
                                //  richDialog.setAffirmativeTextAndAccessKey("&Okidokee");
                                //  richDialog.setCancelTextAndAccessKey("&NoWay");
                                AdfFacesContext.getCurrentInstance().addPartialTarget(richDialog);
                                if (richDialog.getChildCount() == 1 && richDialog.getChildren().get(0) != null &&
                                    richDialog.getChildren().get(0) instanceof RichPanelGridLayout) {
                                    RichPanelGridLayout panelGridLayout =
                                        (RichPanelGridLayout) richDialog.getChildren().get(0);
                                    uiCompList = panelGridLayout.getChildren();
                                    // always gridrows and always two.

                                    if (uiCompList.size() > 0) {
                                        // the first one is the search panel
                                        RichGridRow richGridRow = (RichGridRow) uiCompList.get(0);
                                        if (richGridRow.getChildCount() == 1 &&
                                            richGridRow.getChildren().get(0) != null &&
                                            richGridRow.getChildren().get(0) instanceof RichQuery) {
                                            RichQuery richQuery = (RichQuery) richGridRow.getChildren().get(0);
                                            if (richQuery != null) {
                                                richQuery.setModeChangeVisible(false);
                                                AdfFacesContext.getCurrentInstance().addPartialTarget(richQuery);
                                            }
                                        }
                                        // the second contains the table
                                        richGridRow = (RichGridRow) uiCompList.get(1);
                                        RichGridCell richCell = (RichGridCell) richGridRow.getChildren().get(0);
                                        RichTable theTable = (RichTable) richCell.getChildren().get(0);

                                        theTable.setEmptyText("Really, this is something, but it works");
                                        AdfFacesContext.getCurrentInstance().addPartialTarget(theTable);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void lovLaunch(LaunchPopupEvent launchPopupEvent) {
        RichInputListOfValues inputListOfValues = (RichInputListOfValues) launchPopupEvent.getComponent();
        if (inputListOfValues != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String clientId = inputListOfValues.getClientId(facesContext);
            StringBuffer scriptBuffer = new StringBuffer();
            scriptBuffer.append(" var docComp = AdfPage.PAGE.findComponent(\"f1\"); var comp = AdfPage.PAGE.findComponent(\"");
            scriptBuffer.append(clientId).append("\");  ");
            scriptBuffer.append(" if (comp != null && docComp != null) { var CompId = comp.getId(); ");
            scriptBuffer.append(" AdfCustomEvent.queue(docComp,\"lovafterlaunch\",{compId:CompId},true);}");
            String script = scriptBuffer.toString();
            ExtendedRenderKitService erks =
                Service.getService(facesContext.getRenderKit(), ExtendedRenderKitService.class);
            erks.addScript(facesContext, script);
        }
    }

}
