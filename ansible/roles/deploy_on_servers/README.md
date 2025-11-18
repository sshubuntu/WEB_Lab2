Deploy on servers
=========

I use this role for deploying wildfly lab on linux servers and Vms.

Example Playbook
----------------

Including an example of how to use your role (for instance, with variables passed in as parameters) is always nice for users too:

    - hosts: all
      roles:
        - { role: deploy_on_servers, when ansible_os_family=FreeBSD}

License
-------

BSD

Author Information
------------------
sshubuntu