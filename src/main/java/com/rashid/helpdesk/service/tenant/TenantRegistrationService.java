package com.rashid.helpdesk.service.tenant;

import com.rashid.helpdesk.dto.TenantRegistrationRequest;
import com.rashid.helpdesk.dto.TenantRegistrationResponse;

public interface TenantRegistrationService {
    TenantRegistrationResponse register(TenantRegistrationRequest request);
}
