source setup.sh

expenv -f  hisd3middleware.yml > /tmp/hisd3middleware.yml

oc process -f /tmp/hisd3middleware.yml | oc create  --namespace=${HISD3_MIDDLEWARE_DEPLOYMENT_PROJECT?} -f -