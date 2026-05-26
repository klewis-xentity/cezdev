//--------------------------------------------------------------
// name: CDateTimepickerInstructions
// desc: Date+Time picker using JSpinner + SpinnerDateModel
// cmds: same pattern as datepicker, defaults to "yyyy-MM-dd HH:mm"
//--------------------------------------------------------------
package c3dclasses;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class CDateTimepickerInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CDateTimepickerInstructions(CProcessor cprocessor) {
        super(cprocessor);
        this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CDateTimepickerInstructions _this = this;

        CFunction fnCreate = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
                JSpinner spinner = new JSpinner(model);
                String fmt = _this.getOrDefaultFormat(control, "yyyy-MM-dd HH:mm");
                _this.applyFormat(spinner, fmt);

                CHash params = (CHash) control._("m_params");
                if (params != null) {
                    Object min = params._("m_min");
                    Object max = params._("m_max");
                    if (min != null) model.setStart(_this.coerceToDate(min, fmt));
                    if (max != null) model.setEnd(_this.coerceToDate(max, fmt));
                }
                control._("m_format", fmt);
                m_ccontrolinstructions.createJControl(control, spinner);
                return CReturn._done(control);
            }
        };

        CFunction fnSetValue = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                String fmt = _this.getOrDefaultFormat(c, "yyyy-MM-dd HH:mm");
                Date d = _this.coerceToDate(c._("m_propvalue"), fmt);
                if (d != null) sp.setValue(d);
                return null;
            }
        };

        CFunction fnGetValue = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                String fmt = (String) c._("m_format");
                Object val = sp.getValue();
                if (fmt != null && !fmt.isEmpty() && val instanceof Date) {
                    c._("m_propvalue", new SimpleDateFormat(fmt).format((Date) val));
                } else {
                    c._("m_propvalue", val);
                }
                return null;
            }
        };

        CFunction fnSetFormat = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                String fmt = (String) c._("m_propvalue");
                if (fmt != null && !fmt.isEmpty()) {
                    JSpinner sp = (JSpinner) c._("m_jcontrol");
                    _this.applyFormat(sp, fmt);
                    c._("m_format", fmt);
                }
                return null;
            }
        };

        CFunction fnSetMin = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                SpinnerDateModel model = (SpinnerDateModel) sp.getModel();
                String fmt = _this.getOrDefaultFormat(c, "yyyy-MM-dd HH:mm");
                Date d = _this.coerceToDate(c._("m_propvalue"), fmt);
                model.setStart(d);
                return null;
            }
        };
        CFunction fnSetMax = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                SpinnerDateModel model = (SpinnerDateModel) sp.getModel();
                String fmt = _this.getOrDefaultFormat(c, "yyyy-MM-dd HH:mm");
                Date d = _this.coerceToDate(c._("m_propvalue"), fmt);
                model.setEnd(d);
                return null;
            }
        };

        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");
                Object prev = c._("m_onchange_listener");
                if (prev instanceof ChangeListener) sp.removeChangeListener((ChangeListener) prev);
                if (command != null && !command.trim().isEmpty()) {
                    ChangeListener l = new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            __.exec_command(command + " " + (String) c._("m_strid"));
                        }
                    };
                    sp.addChangeListener(l);
                    c._("m_onchange_listener", l);
                }
                return null;
            }
        };

        // REGISTER
        cprocessor._("datetimepicker->create", fnCreate);
        cprocessor._("datetimepicker->set->value", fnSetValue);
        cprocessor._("datetimepicker->get->value", fnGetValue);
        cprocessor._("datetimepicker->set->format", fnSetFormat);
        cprocessor._("datetimepicker->set->min", fnSetMin);
        cprocessor._("datetimepicker->set->max", fnSetMax);
        cprocessor._("datetimepicker->set->onchange", fnOnChange);

        cprocessor._("datetimepicker->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("datetimepicker->get->visible", cprocessor._("ccontrol->get->visible"));
    }

    private void applyFormat(JSpinner spinner, String pattern) {
        try {
            spinner.setEditor(new JSpinner.DateEditor(spinner, pattern));
        } catch (IllegalArgumentException ex) {
            spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm"));
        }
    }

    private String getOrDefaultFormat(CControl c, String def) {
        Object fmt = c._("m_format");
        if (fmt instanceof String && !((String) fmt).isEmpty()) return (String) fmt;
        CHash params = (CHash) c._("m_params");
        if (params != null) {
            Object p = params._("m_format");
            if (p instanceof String && !((String) p).isEmpty()) return (String) p;
        }
        return def;
    }

    private Date coerceToDate(Object v, String format) {
        if (v == null) return null;
        if (v instanceof Date) return (Date) v;
        if (v instanceof Number) return new Date(((Number) v).longValue());
        if (v instanceof String) {
            String s = (String) v;
            if (s.trim().isEmpty()) return null;
            try { return new SimpleDateFormat(format).parse(s); } catch (ParseException ignored) {}
            String[] fmts = new String[]{
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss",
                "MM/dd/yyyy HH:mm", "MM/dd/yyyy", "yyyy-MM-dd"
            };
            for (String f : fmts) {
                try { return new SimpleDateFormat(f).parse(s); } catch (ParseException ignored2) {}
            }
            try { return new Date(Long.parseLong(s)); } catch (NumberFormatException ignored3) {}
        }
        return null;
    }
}
