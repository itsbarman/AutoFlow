import type { InputHTMLAttributes } from 'react';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  required?: boolean;
}

export function TextField({ label, error, required, id, className = '', ...props }: TextFieldProps) {
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1 block text-sm font-medium text-slate-700">
        {label}
        {required && <span className="ml-0.5 text-red-500">*</span>}
      </label>
      <input
        id={id}
        aria-invalid={Boolean(error)}
        className={`w-full rounded-lg border px-3 py-2 text-sm text-slate-800 shadow-sm transition-colors
          placeholder:text-slate-400 focus:outline-none focus:ring-2
          ${
            error
              ? 'border-red-400 focus:border-red-500 focus:ring-red-200'
              : 'border-slate-300 focus:border-brand-500 focus:ring-brand-100'
          }`}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}
