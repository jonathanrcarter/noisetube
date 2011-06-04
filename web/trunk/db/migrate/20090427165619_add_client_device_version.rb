class AddClientDeviceVersion < ActiveRecord::Migration
  def self.up
    add_column :tracks, :client, :string
    add_column :tracks, :client_version, :string
    add_column :tracks, :device, :string
    add_column :tracks, :created_at, :datetime
    add_column :tracks, :geolocated, :boolean , :default=>false
    remove_column :tracks, :start
  end

  def self.down
  end
end
