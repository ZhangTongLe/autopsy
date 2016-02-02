/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2016 Basis Technology Corp.
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
package org.sleuthkit.autopsy.modules.UserArtifacts;

import com.sun.media.jfxmedia.logging.Logger;
import java.util.List;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.Blackboard.BlackboardException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.BlackboardAttribute.TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Test module that creates new artifact and attribute types.
 */
public class UserArtifactIngestModule implements DataSourceIngestModule {

    private BlackboardArtifact.Type type1, type2;

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        progressBar.switchToDeterminate(2);
        try {
            FileManager manager = Case.getCurrentCase().getServices().getFileManager();
            List<AbstractFile> file1 = manager.findFiles("Sunset.jpg"); //NON-NLS
            List<AbstractFile> file2 = manager.findFiles("Winter.jpg"); //NON-NLS
            BlackboardArtifact art1;
            BlackboardArtifact art2;
            if (!file1.isEmpty()) {
                art1 = file1.get(0).newArtifact(type1.getTypeID());
            } else {
                art1 = dataSource.newArtifact(type1.getTypeID());
            }
            if (!file2.isEmpty()) {
                art2 = file2.get(0).newArtifact(type2.getTypeID());
            } else {
                art2 = dataSource.newArtifact(type2.getTypeID());
            }
            BlackboardAttribute.Type attributeType = Case.getCurrentCase().getServices().getBlackboard().addAttributeType("Test", TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.LONG, "2"); //NON-NLS
            BlackboardAttribute.Type attributeType2 = Case.getCurrentCase().getServices().getBlackboard().addAttributeType("Test2", TSK_BLACKBOARD_ATTRIBUTE_VALUE_TYPE.BYTE, "3"); //NON-NLS
            art1.addAttribute(new BlackboardAttribute(attributeType,
                    UserArtifactIngestModuleFactory.getModuleName(), -1L));
            progressBar.progress(1);
            art2.addAttribute(new BlackboardAttribute(attributeType2,
                    UserArtifactIngestModuleFactory.getModuleName(), new byte[7]));
            progressBar.progress(1);
            IngestServices.getInstance().postMessage(IngestMessage.createDataMessage(
                    "name", // NON-NLS
                    UserArtifactIngestModuleFactory.getModuleName(),
                    "Test Results", //NON-NLS
                    "Test", //NON-NLS
                    art1));
            return ProcessResult.OK;
        } catch (TskCoreException | BlackboardException ex) {
            return ProcessResult.ERROR;
        }
    }

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        try {
            type1 = Case.getCurrentCase().getServices().getBlackboard().addArtifactType("This is", "a test"); //NON-NLS
            type2 = Case.getCurrentCase().getServices().getBlackboard().addArtifactType("Another", "kinda test"); //NON-NLS
        } catch (BlackboardException ex) {
            Logger.logMsg(Logger.ERROR, "Startup failed"); //NON-NLS
        }
    }
}