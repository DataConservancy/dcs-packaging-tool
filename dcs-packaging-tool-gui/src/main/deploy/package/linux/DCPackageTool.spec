Summary: GUI for describing data and generating data packages from file system.
Name: DCPackageTool
Version: 1.0.1
Release: 1
License: Apache License, Version 2.0
Vendor: Data Conservancy <dc-ird@googlegroups.com>
Prefix: /opt
Provides: dc-package-tool
Requires: ld-linux.so.2 libX11.so.6 libXext.so.6 libXi.so.6 libXrender.so.1 libXtst.so.6 libasound.so.2 libc.so.6 libdl.so.2 libgcc_s.so.1 libm.so.6 libpthread.so.0 libthread_db.so.1
Autoprov: 0
Autoreq: 0

#avoid ARCH subfolder
%define _rpmfilename %%{NAME}-%%{VERSION}-%%{RELEASE}.%%{ARCH}.rpm

#comment line below to enable effective jar compression
#it could easily get your package size from 40 to 15Mb but 
#build time will substantially increase and it may require unpack200/system java to install
%define __jar_repack %{nil}

%description
Describe data files on a filesystem and package them.

%prep

%build

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/opt
cp -r %{_sourcedir}/DCPackageTool %{buildroot}/opt

%files

/opt/DCPackageTool

%post
cp /opt/DCPackageTool/DCPackageTool.desktop /usr/share/applications/

%preun
rm -f /usr/share/applications/DCPackageTool.desktop

%clean
