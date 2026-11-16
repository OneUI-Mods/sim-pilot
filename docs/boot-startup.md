# Boot startup control

Missing preference defaults to true. Explicit false is retained. Boot-triggered work checks the preference before it performs recovery, root work, or service start. This controls boot resumption, independently of existing feature switches and manual/on-demand activation. No new worker, alarm or service was added.

Existing update recovery is preserved. Credential-encrypted configuration is only used after unlock; existing device-protected connectivity paths retain their storage behavior. Do not force a reboot or alter user settings merely to validate this option.
