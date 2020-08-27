package ui.controls.lists;

public enum ThreeState {
    True, False, Indeterminate, LockedTrue;

    public static ThreeState valueOf(Boolean value) {
        if(value == null)
            return Indeterminate;
        if(value)
            return True;
        else
            return False;
    }

    public Boolean asBoolean() {
        switch (this) {
            case True:
            case LockedTrue:
                return true;
            case False:  return false;
            case Indeterminate: return null;
        }
        return null;
    }
}
