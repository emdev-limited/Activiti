/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.legacy.deployment;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.activiti.rest.common.api.AbstractPaginateList;

/**
 * @author Tijs Rademakers
 */
@Deprecated
public class LegacyDeploymentsPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<LegacyDeploymentResponse> responseList = new ArrayList<LegacyDeploymentResponse>();
    for (Object deployment : list) {
      responseList.add(new LegacyDeploymentResponse((Deployment) deployment));
    }
    return responseList;
  }
}
