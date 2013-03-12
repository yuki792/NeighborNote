#!/bin/sh

package_dir=$(cd `dirname $0` && pwd)

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

cp $package_dir/usr/share/applications/neighbornote.desktop /usr/share/applications/neighbornote.desktop
mkdir /usr/share/neighbornote
cp -r $package_dir/usr/share/neighbornote/* /usr/share/neighbornote/

echo "Install complete"
