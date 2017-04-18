package org.activiti.engine.impl.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import ru.emdev.activiti.runtime.internal.identity.UserImpl;

public class WorkflowUtil {
	private static Log log = LogFactoryUtil.getLog(WorkflowUtil.class);
	
    public static List<User> findUsersByGroup(long companyId, String groupName) {
		// first - try to parse group to identify - it is regular group or org/community group
		String[] parsedName = groupName.split("/");
		List<com.liferay.portal.kernel.model.User> users = null;
		List<User> result = new ArrayList<User>();
		
		try {
			if (parsedName.length == 1 || Long.valueOf(parsedName[0]) == companyId) {
				if (parsedName.length > 1) {
					groupName = parsedName[1];
					if (parsedName.length > 2) {
						groupName = StringUtils.join(ArrayUtils.subarray(parsedName, 1, parsedName.length), "/");
					}
				}
				// regular group
				Role role = RoleLocalServiceUtil.getRole(companyId, groupName);
				users = UserLocalServiceUtil.getRoleUsers(role.getRoleId());
				
				for (com.liferay.portal.kernel.model.User user : users) {
					result.add(new UserImpl(user));
				}
			} else {
				long groupId = Long.valueOf(parsedName[0]);
				groupName = parsedName[1];
				
				if (parsedName.length > 2) {
					groupName = StringUtils.join(ArrayUtils.subarray(parsedName, 1, parsedName.length), "/");
				}
				
				Role role = RoleLocalServiceUtil.getRole(companyId, groupName);
				List<UserGroupRole> userRoles = UserGroupRoleLocalServiceUtil.getUserGroupRolesByGroupAndRole(groupId, role.getRoleId());
				
				for (UserGroupRole userRole : userRoles) {
					result.add(new UserImpl(userRole.getUser()));
				}
			}
		} catch (Exception ex) {
			log.warn("Cannot get group users", ex);
		}
		
		return result;
	}
}
