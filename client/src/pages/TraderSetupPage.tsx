import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, MapPin, Loader2, ChevronDown, Store, FileText, Navigation, Hash, Sun, Moon, Save, Building, Map, AlignLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { MercotraceIcon } from '@/components/MercotraceLogo';
import { useTheme } from '@/context/ThemeContext';
import { useAuth } from '@/context/AuthContext';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';
import loginBg from '@/assets/login-bg.jpg';

const BUSINESS_CATEGORIES = ['Retailer', 'Exporter', 'Trader', 'Commission Agent', 'Distributor', 'Wholesaler'];
const STATES = ['Karnataka'];
const GSTIN_REGEX = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/;

// Deterministic particles (same pattern as login/register)
const PARTICLES = Array.from({ length: 10 }, (_, i) => ({
  id: i,
  left: `${10 + (i * 8.3) % 85}%`,
  top: `${5 + (i * 13.7) % 85}%`,
  delay: (i * 0.4) % 2,
}));

interface FormData {
  businessName: string;
  businessCategory: string;
  gstin: string;
  address: string;
  shopNo: string;
  state: string;
  market: string;
  description: string;
}

const TraderSetupPage = () => {
  const navigate = useNavigate();
  const { isDark, toggleTheme } = useTheme();
  const { register, isLoading } = useAuth();

  const [form, setForm] = useState<FormData>({
    businessName: '', businessCategory: '', gstin: '', address: '',
    shopNo: '', state: 'Karnataka', market: '', description: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isFetchingLocation, setIsFetchingLocation] = useState(false);
  const [showCategoryDropdown, setShowCategoryDropdown] = useState(false);
  const [showStateDropdown, setShowStateDropdown] = useState(false);

  const inputClass = "pl-12 h-12 sm:h-14 text-base rounded-xl bg-white/90 border-0 text-blue-900 placeholder:text-blue-400";
  const inputClassPlain = "h-12 sm:h-14 text-base rounded-xl bg-white/90 border-0 text-blue-900 placeholder:text-blue-400";

  const updateField = (field: keyof FormData, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors(prev => { const n = { ...prev }; delete n[field]; return n; });
  };

  const handleBlur = (field: string) => {
    setTouched(prev => ({ ...prev, [field]: true }));
    validateField(field);
  };

  const validateField = (field: string): string => {
    let error = '';
    switch (field) {
      case 'businessName': if (!form.businessName.trim()) error = 'This field is required'; break;
      case 'businessCategory': if (!form.businessCategory) error = 'Please select a category'; break;
      case 'gstin': if (form.gstin && !GSTIN_REGEX.test(form.gstin.toUpperCase())) error = 'Invalid GSTIN format'; break;
      case 'address': if (!form.address.trim()) error = 'This field is required'; break;
      case 'shopNo': if (!form.shopNo.trim()) error = 'This field is required'; break;
      case 'state': if (!form.state) error = 'This field is required'; break;
      case 'market': if (!form.market.trim()) error = 'This field is required'; break;
    }
    setErrors(prev => error ? { ...prev, [field]: error } : (() => { const n = { ...prev }; delete n[field]; return n; })());
    return error;
  };

  const validateAll = (): boolean => {
    const fields = ['businessName', 'businessCategory', 'address', 'shopNo', 'state', 'market', 'gstin'];
    let valid = true;
    fields.forEach(f => { if (validateField(f)) valid = false; });
    setTouched(fields.reduce((a, f) => ({ ...a, [f]: true }), {}));
    return valid;
  };

  const handleUseCurrentLocation = useCallback(async () => {
    if (!navigator.geolocation) { toast.error('Geolocation not supported'); return; }
    setIsFetchingLocation(true);
    try {
      const pos = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 });
      });
      const { latitude, longitude } = pos.coords;
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&addressdetails=1`);
      if (!res.ok) throw new Error('Location fetch failure');
      const data = await res.json();
      updateField('address', data.display_name || `${latitude}, ${longitude}`);
      toast.success('Location fetched successfully');
    } catch (err: any) {
      if (err?.code === 1) toast.error('Location permission denied');
      else if (err?.code === 3) toast.error('Location request timed out');
      else toast.error('Could not fetch location. Enter manually.');
    } finally { setIsFetchingLocation(false); }
  }, []);

  const handleSubmit = async () => {
    if (!validateAll()) { toast.error('Please fill all required fields'); return; }
    setIsSubmitting(true);
    try {
      await register({
        businessName: form.businessName.trim(), ownerName: form.businessName.trim(),
        mobile: '', email: `trader_${Date.now()}@mercotrace.app`, password: 'trader123456',
        address: form.address.trim(), city: form.market.trim(), state: form.state, pinCode: '', category: form.businessCategory,
      });
      toast.success('Trader profile saved successfully!');
      navigate('/home', { replace: true });
    } catch { toast.error('Failed to save profile. Please try again.'); }
    finally { setIsSubmitting(false); }
  };

  const FieldError = ({ field }: { field: string }) => {
    if (!touched[field] || !errors[field]) return null;
    return <motion.p initial={{ opacity: 0, y: -4 }} animate={{ opacity: 1, y: 0 }} className="text-xs text-red-200 mt-1 ml-1" role="alert">{errors[field]}</motion.p>;
  };

  return (
    <div className="h-[100dvh] relative overflow-hidden flex flex-col">
      {/* Background — identical to login/register */}
      <img src={loginBg} alt="" className="absolute inset-0 w-full h-full object-cover z-0" fetchPriority="high" decoding="async" width={1920} height={1080} />
      <div className="absolute inset-0 bg-gradient-to-br from-blue-900/75 via-blue-800/65 to-violet-900/75 z-[1]" />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.15)_0%,transparent_50%)] z-[1]" />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_70%_80%,rgba(123,97,255,0.2)_0%,transparent_40%)] z-[1]" />

      {/* Particles */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none z-[2]" aria-hidden="true">
        {PARTICLES.map(p => (
          <motion.div key={p.id} className="absolute w-1.5 h-1.5 bg-white/25 rounded-full"
            style={{ left: p.left, top: p.top }}
            animate={{ y: [-15, 15], opacity: [0.15, 0.5, 0.15] }}
            transition={{ duration: 4, repeat: Infinity, delay: p.delay }}
          />
        ))}
      </div>

      {/* Content */}
      <div className="relative z-10 flex-1 flex flex-col min-h-0">
        {/* Top bar */}
        <div className="flex items-center justify-between px-5 pt-[max(1rem,env(safe-area-inset-top))]">
          <button onClick={() => navigate('/login')} className="w-11 h-11 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center text-white border border-white/20" aria-label="Go back">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <button onClick={toggleTheme} className="w-11 h-11 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center text-white border border-white/20" aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}>
            {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
          </button>
        </div>

        {/* Scrollable form area */}
        <div className="flex-1 flex flex-col items-center overflow-y-auto px-6 py-4 no-scrollbar">
          {/* Logo */}
          <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ type: 'spring', damping: 15 }} className="relative mb-4">
            <div className="w-16 h-16 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center shadow-2xl border border-white/20">
              <MercotraceIcon size={32} color="white" className="drop-shadow-lg" />
            </div>
          </motion.div>

          <h1 className="text-2xl font-bold text-white mb-1 drop-shadow-lg">Trader Setup</h1>
          <p className="text-white/70 text-sm mb-5">Set up your business profile to get started</p>

          {/* Form */}
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="w-full max-w-sm space-y-3 pb-28">

            {/* Section: Business Details */}
            <p className="text-xs font-semibold text-white/50 uppercase tracking-wider pt-1 flex items-center gap-1.5">
              <Store className="w-3.5 h-3.5" /> Business Details
            </p>

            {/* Business Name */}
            <div>
              <div className="relative">
                <Building className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-blue-800/50" />
                <Input placeholder="Business Name *" value={form.businessName} onChange={e => updateField('businessName', e.target.value)} onBlur={() => handleBlur('businessName')}
                  className={cn(inputClass, touched.businessName && errors.businessName && "ring-2 ring-red-400/50")} maxLength={100} />
              </div>
              <FieldError field="businessName" />
            </div>

            {/* Business Category Dropdown */}
            <div className="relative">
              <button type="button"
                onClick={() => { setShowCategoryDropdown(!showCategoryDropdown); setShowStateDropdown(false); }}
                onBlur={() => setTimeout(() => { setShowCategoryDropdown(false); handleBlur('businessCategory'); }, 150)}
                className={cn("w-full h-12 sm:h-14 px-4 rounded-xl bg-white/90 text-sm flex items-center justify-between",
                  form.businessCategory ? 'text-blue-900' : 'text-blue-400',
                  touched.businessCategory && errors.businessCategory && "ring-2 ring-red-400/50"
                )}>
                <span>{form.businessCategory || 'Select Business Category *'}</span>
                <ChevronDown className={cn('w-4 h-4 text-blue-800/50 transition-transform', showCategoryDropdown && 'rotate-180')} />
              </button>
              {showCategoryDropdown && (
                <motion.div initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }}
                  className="absolute z-50 top-full mt-2 w-full rounded-2xl py-2 max-h-48 overflow-auto bg-white/95 backdrop-blur-xl shadow-xl border border-white/50">
                  {BUSINESS_CATEGORIES.map(cat => (
                    <button key={cat} type="button" onMouseDown={e => e.preventDefault()}
                      onClick={() => { updateField('businessCategory', cat); setShowCategoryDropdown(false); }}
                      className={cn("w-full text-left px-4 py-2.5 text-sm text-blue-900 hover:bg-blue-50 transition-colors",
                        form.businessCategory === cat && "bg-blue-50 font-medium"
                      )}>{cat}</button>
                  ))}
                </motion.div>
              )}
              <FieldError field="businessCategory" />
            </div>

            {/* GSTIN */}
            <div>
              <div className="relative">
                <FileText className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-blue-800/50" />
                <Input placeholder="GSTIN (optional)" value={form.gstin} onChange={e => updateField('gstin', e.target.value.toUpperCase())} onBlur={() => handleBlur('gstin')}
                  className={cn(inputClass, "uppercase", touched.gstin && errors.gstin && "ring-2 ring-red-400/50")} maxLength={15} />
              </div>
              <FieldError field="gstin" />
            </div>

            {/* Section: Address & Location */}
            <p className="text-xs font-semibold text-white/50 uppercase tracking-wider pt-3 flex items-center gap-1.5">
              <MapPin className="w-3.5 h-3.5" /> Address & Location
            </p>

            {/* Address */}
            <div>
              <div className="relative">
                <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-blue-800/50" />
                <Input placeholder="Search or enter address *" value={form.address} onChange={e => updateField('address', e.target.value)} onBlur={() => handleBlur('address')}
                  className={cn(inputClass, touched.address && errors.address && "ring-2 ring-red-400/50")} />
              </div>
              <FieldError field="address" />
              <button type="button" onClick={handleUseCurrentLocation} disabled={isFetchingLocation}
                className="mt-1.5 flex items-center gap-2 text-sm text-white/80 font-medium hover:text-white transition-colors min-h-[44px] px-1">
                {isFetchingLocation ? <Loader2 className="w-4 h-4 animate-spin" /> : <Navigation className="w-4 h-4" />}
                {isFetchingLocation ? 'Fetching location...' : '📍 Use my current location as address'}
              </button>
            </div>

            {/* Shop No */}
            <div>
              <div className="relative">
                <Hash className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-blue-800/50" />
                <Input placeholder="Shop No *" value={form.shopNo} onChange={e => updateField('shopNo', e.target.value)} onBlur={() => handleBlur('shopNo')}
                  className={cn(inputClass, touched.shopNo && errors.shopNo && "ring-2 ring-red-400/50")} maxLength={20} />
              </div>
              <FieldError field="shopNo" />
            </div>

            {/* Section: Market Location */}
            <p className="text-xs font-semibold text-white/50 uppercase tracking-wider pt-3 flex items-center gap-1.5">
              <Map className="w-3.5 h-3.5" /> Your Market Location
            </p>

            {/* State Dropdown */}
            <div className="relative">
              <button type="button"
                onClick={() => { setShowStateDropdown(!showStateDropdown); setShowCategoryDropdown(false); }}
                onBlur={() => setTimeout(() => { setShowStateDropdown(false); handleBlur('state'); }, 150)}
                className={cn("w-full h-12 sm:h-14 px-4 rounded-xl bg-white/90 text-sm flex items-center justify-between text-blue-900")}>
                <span>{form.state || 'Select State *'}</span>
                <ChevronDown className={cn('w-4 h-4 text-blue-800/50 transition-transform', showStateDropdown && 'rotate-180')} />
              </button>
              {showStateDropdown && (
                <motion.div initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }}
                  className="absolute z-50 top-full mt-2 w-full rounded-2xl py-2 bg-white/95 backdrop-blur-xl shadow-xl border border-white/50">
                  {STATES.map(s => (
                    <button key={s} type="button" onMouseDown={e => e.preventDefault()}
                      onClick={() => { updateField('state', s); setShowStateDropdown(false); }}
                      className={cn("w-full text-left px-4 py-2.5 text-sm text-blue-900 hover:bg-blue-50 transition-colors",
                        form.state === s && "bg-blue-50 font-medium"
                      )}>{s}</button>
                  ))}
                </motion.div>
              )}
              <FieldError field="state" />
            </div>

            {/* Market */}
            <div>
              <div className="relative">
                <Store className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-blue-800/50" />
                <Input placeholder="Market Name *" value={form.market} onChange={e => updateField('market', e.target.value)} onBlur={() => handleBlur('market')}
                  className={cn(inputClass, touched.market && errors.market && "ring-2 ring-red-400/50")} maxLength={100} />
              </div>
              <FieldError field="market" />
            </div>

            {/* Section: Description */}
            <p className="text-xs font-semibold text-white/50 uppercase tracking-wider pt-3 flex items-center gap-1.5">
              <AlignLeft className="w-3.5 h-3.5" /> Additional Info
            </p>

            <textarea
              placeholder="Tell us about your shop... (optional)"
              value={form.description}
              onChange={e => updateField('description', e.target.value)}
              className="w-full min-h-[100px] px-4 py-3 rounded-xl bg-white/90 border-0 text-sm text-blue-900 placeholder:text-blue-400 resize-none focus:outline-none focus:ring-2 focus:ring-white/50"
              maxLength={500}
            />

            {/* Submit button */}
            <Button onClick={handleSubmit} disabled={isSubmitting || isLoading}
              className="w-full h-12 sm:h-14 rounded-xl text-base sm:text-lg font-semibold bg-white text-blue-600 hover:bg-white/90 shadow-xl disabled:opacity-70 mt-2">
              {isSubmitting ? (
                <motion.div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full" animate={{ rotate: 360 }} transition={{ duration: 1, repeat: Infinity, ease: 'linear' }} />
              ) : (
                <><Save className="w-5 h-5 mr-2" /> Save & Continue</>
              )}
            </Button>
          </motion.div>
        </div>
      </div>
    </div>
  );
};

export default TraderSetupPage;
