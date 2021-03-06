/*
 * Central Repository
 *
 * Copyright 2018 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.centralrepository.contentviewer;

import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.centralrepository.datamodel.CorrelationAttribute;
import org.sleuthkit.autopsy.centralrepository.datamodel.CorrelationAttributeInstance;
import org.sleuthkit.autopsy.centralrepository.datamodel.EamDbException;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.DataSource;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;
import org.sleuthkit.datamodel.TskDataException;

/**
 * Class for populating the Other Occurrences tab
 */
class OtherOccurrenceNodeData {
    
    // For now hard code the string for the central repo files type, since 
    // getting it dynamically can fail.
    private static final String FILE_TYPE_STR = "Files";
    
    private final String caseName;
    private String deviceID;
    private String dataSourceName;
    private final String filePath;
    private final String typeStr;
    private final CorrelationAttribute.Type type;
    private final String value;
    private TskData.FileKnown known;
    private String comment;
    
    private AbstractFile originalAbstractFile = null;
    private CorrelationAttributeInstance originalCorrelationInstance = null;
    
    /**
     * Create a node from a central repo instance.
     * @param instance The central repo instance
     * @param type     The type of the instance
     * @param value    The value of the instance
     */
    OtherOccurrenceNodeData(CorrelationAttributeInstance instance, CorrelationAttribute.Type type, String value) {
        caseName = instance.getCorrelationCase().getDisplayName();
        deviceID = instance.getCorrelationDataSource().getDeviceID();
        dataSourceName = instance.getCorrelationDataSource().getName();
        filePath = instance.getFilePath();
        this.typeStr = type.getDisplayName();
        this.type = type;
        this.value = value;
        known = instance.getKnownStatus();
        comment = instance.getComment();
        
        originalCorrelationInstance = instance;
    }
    
    /**
     * Create a node from an abstract file.
     * @param newFile     The abstract file
     * @param autopsyCase The current case
     * @throws EamDbException 
     */
    OtherOccurrenceNodeData(AbstractFile newFile, Case autopsyCase) throws EamDbException {
        caseName = autopsyCase.getDisplayName();
        try {
            DataSource dataSource = autopsyCase.getSleuthkitCase().getDataSource(newFile.getDataSource().getId());
            deviceID = dataSource.getDeviceId();
            dataSourceName = dataSource.getName();
        } catch (TskDataException | TskCoreException ex) {
            throw new EamDbException("Error loading data source for abstract file ID " + newFile.getId(), ex);
        }
        
        filePath = newFile.getParentPath() + newFile.getName();
        typeStr = FILE_TYPE_STR;
        this.type = null;
        value = newFile.getMd5Hash();
        known = newFile.getKnown();
        comment = "";
        
        originalAbstractFile = newFile;
    }
    
    /**
     * Check if this node is a "file" type
     * @return true if it is a file type
     */
    boolean isFileType() {
        return FILE_TYPE_STR.equals(typeStr);
    }
    
    /**
     * Update the known status for this node
     * @param newKnownStatus The new known status
     */
    void updateKnown(TskData.FileKnown newKnownStatus) {
        known = newKnownStatus;
    }
    
    /**
     * Update the comment for this node
     * @param newComment The new comment
     */
    void updateComment(String newComment) {
        comment = newComment;
    }
    
    /**
     * Check if this is a central repo node.
     * @return true if this node was created from a central repo instance, false otherwise
     */
    boolean isCentralRepoNode() {
        return (originalCorrelationInstance != null);
    }
    
    /**
     * Uses the saved instance plus type and value to make a new CorrelationAttribute.
     * Should only be called if isCentralRepoNode() is true.
     * @return the newly created CorrelationAttribute
     */
    CorrelationAttribute createCorrelationAttribute() throws EamDbException {
        if (! isCentralRepoNode() ) { 
            throw new EamDbException("Can not create CorrelationAttribute for non central repo node");
        }
        CorrelationAttribute attr = new CorrelationAttribute(type, value);
        attr.addInstance(originalCorrelationInstance);
        return attr;
    }
    
    /**
     * Get the case name
     * @return the case name
     */
    String getCaseName() {
        return caseName;
    }
    
    /**
     * Get the device ID
     * @return the device ID
     */
    String getDeviceID() {
        return deviceID;
    }
    
    /**
     * Get the data source name
     * @return the data source name
     */
    String getDataSourceName() {
        return dataSourceName;
    }
    
    /**
     * Get the file path
     * @return the file path
     */
    String getFilePath() {
        return filePath;
    }
    
    /**
     * Get the type (as a string)
     * @return the type
     */
    String getType() {
        return typeStr;
    }
    
    /**
     * Get the value (MD5 hash for files)
     * @return the value
     */
    String getValue() {
        return value;
    }
    
    /**
     * Get the known status
     * @return the known status
     */
    TskData.FileKnown getKnown() {
        return known;
    }
    
    /**
     * Get the comment
     * @return the comment
     */
    String getComment() {
        return comment;
    }
    
    /**
     * Get the backing abstract file.
     * Should only be called if isCentralRepoNode() is false
     * @return the original abstract file
     */
    AbstractFile getAbstractFile() throws EamDbException {
        if (originalCorrelationInstance == null) {
            throw new EamDbException("AbstractFile is null");
        }
        return originalAbstractFile;
    }
    
    /**
     * Get the backing CorrelationAttributeInstance.
     * Should only be called if isCentralRepoNode() is true
     * @return the original CorrelationAttributeInstance
     * @throws EamDbException 
     */
    CorrelationAttributeInstance getCorrelationAttributeInstance() throws EamDbException {
        if (originalCorrelationInstance == null) {
            throw new EamDbException("CorrelationAttributeInstance is null");
        }
        return originalCorrelationInstance;
    }
}
