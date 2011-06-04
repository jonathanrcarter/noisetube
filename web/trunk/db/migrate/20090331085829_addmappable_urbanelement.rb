class AddmappableUrbanelement < ActiveRecord::Migration
  def self.up
    add_column :cities, :lat, :float
    add_column :cities, :lng, :float
    add_column :cities, :country, :string
  end

  def self.down
  end
end
