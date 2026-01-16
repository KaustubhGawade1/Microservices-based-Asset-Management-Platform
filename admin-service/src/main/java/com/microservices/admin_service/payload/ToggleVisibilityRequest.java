package com.microservices.admin_service.payload;

public class ToggleVisibilityRequest {
    private boolean makePublic;

    public boolean isMakePublic() {
        return makePublic;
    }

    public void setMakePublic(boolean makePublic) {
        this.makePublic = makePublic;
    }
}
