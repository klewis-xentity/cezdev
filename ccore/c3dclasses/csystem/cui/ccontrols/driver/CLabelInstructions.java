package c3dclasses;

import javax.swing.JLabel;

class CLabelInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CLabelInstructions(CProcessor cprocessor) {
        super(cprocessor);

        final CControlInstructions ccontrolinstructions =
            (CControlInstructions) cprocessor.getCInstructions("CControlInstructions");
        CFunction fnCreateJLabel = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                ccontrolinstructions.createJControl(control, new JLabel((String) control._("m_value")));
                return CReturn._done(control);
            }
        };

        cprocessor._("label->create", fnCreateJLabel);
        cprocessor._("label->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("label->get->visible", cprocessor._("ccontrol->get->visible"));
        cprocessor._("label->set->font", cprocessor._("ccontrol->set->font"));
        cprocessor._("label->get->font", cprocessor._("ccontrol->get->font"));
        cprocessor._("label->set->enable", cprocessor._("ccontrol->set->enable"));
        cprocessor._("label->get->enable", cprocessor._("ccontrol->get->enable"));
        cprocessor._("label->set->icon", cprocessor._("ccontrol->set->icon"));
        cprocessor._("label->get->icon", cprocessor._("ccontrol->get->icon"));
        cprocessor._("label->set->text", cprocessor._("ccontrol->set->text"));
        cprocessor._("label->get->text", cprocessor._("ccontrol->get->text"));
        cprocessor._("label->set->title", cprocessor._("ccontrol->set->text"));
        cprocessor._("label->get->title", cprocessor._("ccontrol->get->text"));
        cprocessor._("label->set->horizontalalignment", cprocessor._("ccontrol->set->horizontalalignment"));
        cprocessor._("label->get->horizontalalignment", cprocessor._("ccontrol->get->horizontalalignment"));
        cprocessor._("label->set->verticalalignment", cprocessor._("ccontrol->set->verticalalignment"));
        cprocessor._("label->get->verticalalignment", cprocessor._("ccontrol->get->verticalalignment"));
    }
}
