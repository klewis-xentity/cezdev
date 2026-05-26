//--------------------------------------------------------------
// name: CDatepickerInstructions
// desc: Date-only picker using JSpinner + SpinnerDateModel
// cmds:
//   datepicker->create
//   datepicker->set->value         (Date | Long epoch | String formatted)
//   datepicker->get->value         (returns String if format set, else Date)
//   datepicker->set->format        (e.g., "yyyy-MM-dd")
//   datepicker->set->min           (Date | Long | String)
//   datepicker->set->max           (Date | Long | String)
//   datepicker->set->onchange      (command string; executes on change)
//   datepicker->set->visible / get->visible (via ccontrol generic)
// params on create (optional): m_format (String), m_min, m_max
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

class CDatepickerInstructions extends CInstructions {
    CControlInstructions m_ccontrolinstructions;

    public CDatepickerInstructions(CProcessor cprocessor) {
        super(cprocessor);
        this.m_ccontrolinstructions = (CControlInstructions) cprocessor.getCInstructions("CControlInstrunctions");
        final CDatepickerInstructions _this = this;

        // CREATE
        CFunction fnCreate = new CFunction() {
            public CReturn call(CObject obj) {
                CControl control = (CControl) obj;
                SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
                JSpinner spinner = new JSpinner(model);
                // default format & apply
                String fmt = _this.getOrDefaultFormat(control, "yyyy-MM-dd");
                _this.applyFormat(spinner, fmt);
                // optional min/max from params
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

        // SET VALUE
        CFunction fnSetValue = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                String fmt = _this.getOrDefaultFormat(c, "yyyy-MM-dd");
                Date d = _this.coerceToDate(c._("m_propvalue"), fmt);
                if (d != null) sp.setValue(d);
                return null;
            }
        };

        // GET VALUE
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

        // SET FORMAT
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

        // SET MIN/MAX
        CFunction fnSetMin = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                SpinnerDateModel model = (SpinnerDateModel) sp.getModel();
                String fmt = _this.getOrDefaultFormat(c, "yyyy-MM-dd");
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
                String fmt = _this.getOrDefaultFormat(c, "yyyy-MM-dd");
                Date d = _this.coerceToDate(c._("m_propvalue"), fmt);
                model.setEnd(d);
                return null;
            }
        };

        // ONCHANGE
        CFunction fnOnChange = new CFunction() {
            public CReturn call(CObject obj) {
                CControl c = (CControl) obj;
                JSpinner sp = (JSpinner) c._("m_jcontrol");
                final String command = (String) c._("m_propvalue");
                // Remove previous
                Object prev = c._("m_onchange_listener");
                if (prev instanceof ChangeListener) {
                    sp.removeChangeListener((ChangeListener) prev);
                }
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
        cprocessor._("datepicker->create", fnCreate);
        cprocessor._("datepicker->set->value", fnSetValue);
        cprocessor._("datepicker->get->value", fnGetValue);
        cprocessor._("datepicker->set->format", fnSetFormat);
        cprocessor._("datepicker->set->min", fnSetMin);
        cprocessor._("datepicker->set->max", fnSetMax);
        cprocessor._("datepicker->set->onchange", fnOnChange);

        // generic visibility mapping
        cprocessor._("datepicker->set->visible", cprocessor._("ccontrol->set->visible"));
        cprocessor._("datepicker->get->visible", cprocessor._("ccontrol->get->visible"));
    }

    // helpers
    private void applyFormat(JSpinner spinner, String pattern) {
        try {
            spinner.setEditor(new JSpinner.DateEditor(spinner, pattern));
        } catch (IllegalArgumentException ex) {
            // fallback
            spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
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
            // Try provided format first
            try { return new SimpleDateFormat(format).parse(s); } catch (ParseException ignored) {}
            // Try common fallbacks
            String[] fmts = new String[]{
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss",
                "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd"
            };
            for (String f : fmts) {
                try { return new SimpleDateFormat(f).parse(s); } catch (ParseException ignored2) {}
            }
            // As epoch string if numeric
            try { return new Date(Long.parseLong(s)); } catch (NumberFormatException ignored3) {}
        }
        return null;
    }
}
